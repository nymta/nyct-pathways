package com.nyct.dos.tpc.pathways;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.nyct.dos.tpc.pathways.model.Connection;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.model.StationComplex;
import com.nyct.dos.tpc.pathways.model.edges.*;
import com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension;
import com.nyct.dos.tpc.pathways.model.extensions.MtaEquipmentIdExtension;
import com.nyct.dos.tpc.pathways.model.nodes.Entrance;
import com.nyct.dos.tpc.pathways.model.nodes.Mezzanine;
import com.nyct.dos.tpc.pathways.model.nodes.Platform;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import com.nyct.dos.tpc.pathways.types.FareControlAreaType;
import com.nyct.dos.tpc.pathways.types.NodeType;
import com.nyct.dos.tpc.pathways.types.PathwayType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension.IS_ACCESSIBLE;
import static com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension.IS_NOT_ACCESSIBLE;
import static com.nyct.dos.tpc.pathways.types.NodeType.*;
import static com.nyct.dos.tpc.pathways.types.PathwayType.*;

@Slf4j
public class NyctPathwaysTransformStrategy implements GtfsTransformStrategy {

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void updateWriteSchema(DefaultEntitySchemaFactory factory) {
        factory.addExtension(Pathway.class, MtaEquipmentIdExtension.class);
        factory.addExtension(Pathway.class, IsAccessibleExtension.class);
    }

    @Setter
    @CsvField
    private File basePath;

    @Setter
    @CsvField
    private File platformStopMappingFile;

    @Setter
    @CsvField
    private File stationComplexesFile;

    @Setter
    @CsvField
    private File stationsFile;

    @CsvField(ignore = true)
    private GtfsMutableRelationalDao dao;

    @CsvField(ignore = true)
    private TransformContext transformContext;

    @CsvField(ignore = true)
    private PathwaysData pd;

    @CsvField(ignore = true)
    private Multimap<Pair<Integer, String>, String> platformStopMapping;

    @CsvField(ignore = true)
    private final AtomicInteger pathwayCount = new AtomicInteger();

    public void run(TransformContext transformContext, GtfsMutableRelationalDao dao) {
        this.transformContext = transformContext;
        this.dao = dao;

        PathwaysLoader pl = new PathwaysLoader(basePath);

        try {
            pd = pl.load();

            platformStopMapping = ReferenceDataLoader.loadPlatformStopMapping(platformStopMappingFile);
            final Map<String, ReferenceStation> stations = ReferenceDataLoader.loadStationsFile(stationsFile);
            final Map<Integer, ReferenceStationComplex> stationComplexes = ReferenceDataLoader.loadStationComplexesFile(stationComplexesFile);

            final Map<Integer, String> stationComplexNames = ReferenceDataLoader.buildStationComplexNames(stations.values(), stationComplexes.values());

            for (StationComplex stationComplex : pd.getStationComplexes()) {
                int stationComplexId = stationComplex.getStationComplexId();

                Stop sc = new Stop();
                sc.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), String.format("MR%03d", stationComplexId)));
                sc.setName(stationComplexNames.get(stationComplexId));
                sc.setLocationType(Stop.LOCATION_TYPE_STATION);
                dao.saveOrUpdateEntity(sc);

                final SetMultimap<Pair<NodeType, Integer>, Stop> stopsForNodes = pd.getConnectionsByComplex().get(stationComplexId)
                        .stream()
                        .flatMap(connection -> Stream.of(from(connection), to(connection)))
                        .distinct()
                        .collect(
                                flatteningToImmutableSetMultimap(
                                        Function.identity(),
                                        p -> this.gtfsStopForNode(stationComplexId, p, sc)
                                )
                        );

                stopsForNodes.values()
                        .forEach(dao::saveOrUpdateEntity);

                for (Connection connection : pd.getConnectionsByComplex().get(stationComplexId)) {
                    final Set<Stop> fromStops = stopsForNodes.get(from(connection));
                    final Set<Stop> toStops = stopsForNodes.get(to(connection));

                    for (Stop fromStop : fromStops) {
                        for (Stop toStop : toStops) {

                            if (connection.getPathwayType() != FARE_CONTROL) {
                                createPathway(connection, fromStop, toStop, connection.getPathwayType().getGtfsMode(), false);
                            } else if (connection.getPathwayType() == FARE_CONTROL && !isFareControlExitOnly(connection)) {
                                //unpaid side is always the from, except if it's exit-only!

                                createPathway(connection, fromStop, toStop, Pathway.MODE_FAREGATE, false);
                                createPathway(connection, toStop, fromStop, Pathway.MODE_EXIT_GATE, true);

                            } else if (connection.getPathwayType() == FARE_CONTROL && isFareControlExitOnly(connection)) {
                                createPathway(connection, fromStop, toStop, Pathway.MODE_EXIT_GATE, false);
                            } else {
                                throw new IllegalStateException(connection.toString()); //FIXME: useful message
                            }

                        }
                    }
                }
            }

            Set<Transfer> transfersToDelete = pd.getStationComplexes().stream()
                    .flatMap(sc -> stations
                            .values()
                            .stream()
                            .filter(s -> s.getStationComplexId() == sc.getStationComplexId()))
                    .map(ReferenceStation::getGtfsStopId)
                    .flatMap(stopId -> Stream.concat(
                            Stream.of(stopId),
                            dao.getAllStops().stream()
                                    .filter(s -> s.getParentStation() != null)
                                    .filter(s -> s.getParentStation().equals(stopId))
                                    .map(Stop::getId)
                                    .map(AgencyAndId::getId)
                    ))
                    .peek(System.out::println)
                    .flatMap(stopId -> dao.getAllTransfers().stream()
                            .filter(t -> t.getFromStop().getId().getId().equals(stopId) || t.getToStop().getId().getId().equals(stopId))
                    )
                    .collect(toImmutableSet());

            transfersToDelete.forEach(dao::removeEntity);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private static Pair<NodeType, Integer> from(@NotNull Connection connection) {
        return ImmutablePair.of(connection.getConnectFromType(), connection.getConnectFromId());
    }

    @NotNull
    private static Pair<NodeType, Integer> to(@NotNull Connection connection) {
        return ImmutablePair.of(connection.getConnectToType(), connection.getConnectToId());
    }

    private void createPathway(Connection connection, Stop fromStop, Stop toStop, int pathwayMode, boolean swapFlag) {
        final Pathway p = new Pathway();

        p.setId(new AgencyAndId(transformContext.getDefaultAgencyId(),
                String.format("MR%03d-C%03d-P%04d",
                        connection.getStationComplexId(), connection.getConnectionId(), pathwayCount.incrementAndGet())));

        p.setPathwayMode(pathwayMode);

        p.setFromStop(fromStop);
        p.setToStop(toStop);

        if (swapFlag) {
            p.setReversedSignpostedAs(connection.getSignpostedAsFrom().trim());
            p.setSignpostedAs(connection.getSignpostedAsTo().trim());
        } else {
            p.setSignpostedAs(connection.getSignpostedAsFrom().trim());
            p.setReversedSignpostedAs(connection.getSignpostedAsTo().trim());
        }

        final MtaEquipmentIdExtension equipmentId = new MtaEquipmentIdExtension();
        final IsAccessibleExtension isAccessible = new IsAccessibleExtension();

        final PathwayType pt = connection.getPathwayType();

        if (pt == STAIR) {
            Stair s = pd.getStairsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(s.getNyctEamStairId().trim());
            isAccessible.setIsAccessible(IS_NOT_ACCESSIBLE);
        } else if (pt == WALKWAY) {
            Walkway w = pd.getWalkwaysByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(w.getNyctWalkwayId().trim());
            isAccessible.setIsAccessible(IS_ACCESSIBLE);
        } else if (pt == ELEVATOR) {
            Elevator e = pd.getElevatorsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(e.getNyctElevatorId().trim());
            isAccessible.setIsAccessible(IS_ACCESSIBLE);
        } else if (pt == ESCALATOR) {
            Escalator e = pd.getEscalatorsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(e.getNyctEscalatorId().trim());
            isAccessible.setIsAccessible(IS_NOT_ACCESSIBLE);
        } else if (pt == FARE_CONTROL) {
            FareControlArea fca = pd.getFareControlAreasByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(fca.getNyctFareControlAreaId().trim());
            isAccessible.setIsAccessible(fca.getAutogate().equals("1") ? IS_ACCESSIBLE : IS_NOT_ACCESSIBLE);
        }

        p.putExtension(MtaEquipmentIdExtension.class, equipmentId);
        p.putExtension(IsAccessibleExtension.class, isAccessible);

        p.setIsBidirectional(connection.getPathwayType() != FARE_CONTROL ? 1 : 0);

        if (p.getIsBidirectional() == 0) {
            p.setReversedSignpostedAs(null);
        }

        dao.saveOrUpdateEntity(p);
    }

    @NotNull
    private static Pair<Integer, Integer> complexAndId(@NotNull Connection connection) {
        return ImmutablePair.of(
                connection.getStationComplexId(),
                Integer.valueOf(connection.getPathwayId().trim())
        );
    }

    private boolean isFareControlExitOnly(@NotNull Connection connection) {
        assert connection.getPathwayType() == FARE_CONTROL;
        FareControlArea fca = pd.getFareControlAreasByComplexAndId().get(complexAndId(connection));

        return fca.getType().equals(EnumSet.of(FareControlAreaType.HXT));
    }

    private static String idForNode(int stationComplexId, NodeType nt, int nodeId) {
        return String.format("MR%03d-%s-%03d", stationComplexId, nt.name(), nodeId);
    }

    private Stop getStop(AgencyAndId id) {
        Stop s = dao.getStopForId(id);
        if (s == null) {
            throw new RuntimeException("Failed to get stop with ID: " + id.toString());
        }
        return s;
    }

    private Stream<Stop> gtfsStopForNode(int stationComplexId, Pair<NodeType, Integer> nodeTypeAndId, Stop stationComplex) {
        NodeType nt = nodeTypeAndId.getLeft();
        int nodeId = nodeTypeAndId.getRight();

        if (nt == ENTRANCE) {
            final Entrance e = pd.getEntrancesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

            if (e == null) {
                log.warn("Cannot find entrance {} at station complex {}", nodeId, stationComplexId);
                return Stream.empty();
            }

            final Stop s = new Stop();

            s.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), idForNode(stationComplexId, nt, nodeId)));
            s.setLocationType(Stop.LOCATION_TYPE_ENTRANCE_EXIT);
            s.setLat(e.getLatitude());
            s.setLon(e.getLongitude());
            s.setName(e.getDescription().trim());
            s.setParentStation(stationComplex.getId().getId());

            return Stream.of(s);

        } else if (nt == PLATFORM) {
            final Platform p = pd.getPlatformsByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

            if (p == null) {
                log.warn("Cannot find platform {} at station complex {}", nodeId, stationComplexId);
                return Stream.empty();
            }

            final Collection<String> gtfsStopIds = platformStopMapping.get(ImmutablePair.of(p.getStationComplexId(), p.getNyctPlatformId()));

            return gtfsStopIds.stream()
                    .map(stopId -> new AgencyAndId(transformContext.getDefaultAgencyId(), stopId))
                    .map(this::getStop);

        } else if (nt == MEZZANINE) {
            final Mezzanine m = pd.getMezzaninesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

            if (m == null) {
                log.warn("Cannot find mezzanine {} at station complex {}", nodeId, stationComplexId);
                return Stream.empty();
            }

            final Stop s = new Stop();

            s.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), idForNode(stationComplexId, nt, nodeId)));
            s.setLocationType(Stop.LOCATION_TYPE_NODE);
            s.setName(m.getDescription().trim());
            s.setParentStation(stationComplex.getId().getId());

            return Stream.of(s);
        } else {
            throw new IllegalArgumentException(nt.name());
        }
    }

}
