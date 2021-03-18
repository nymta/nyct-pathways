package com.nyct.dos.tpc.pathways;

import com.google.common.collect.Multimap;
import com.google.common.collect.SetMultimap;
import com.nyct.dos.tpc.pathways.model.Connection;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.model.StationComplex;
import com.nyct.dos.tpc.pathways.model.edges.*;
import com.nyct.dos.tpc.pathways.model.eerms.Equipment;
import com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension;
import com.nyct.dos.tpc.pathways.model.extensions.MtaEquipmentIdExtension;
import com.nyct.dos.tpc.pathways.model.nodes.Entrance;
import com.nyct.dos.tpc.pathways.model.nodes.Mezzanine;
import com.nyct.dos.tpc.pathways.model.nodes.Platform;
import com.nyct.dos.tpc.pathways.model.referencedata.PlatformStopMapping;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import com.nyct.dos.tpc.pathways.types.FareControlAreaType;
import com.nyct.dos.tpc.pathways.types.NodeType;
import com.nyct.dos.tpc.pathways.types.PathwayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.locationtech.jts.geom.Point;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.model.Transfer;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
import static com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension.IS_ACCESSIBLE;
import static com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension.IS_NOT_ACCESSIBLE;
import static com.nyct.dos.tpc.pathways.types.PathwayType.*;

@RequiredArgsConstructor
@Slf4j
public class PathwaysTransformer {
    private final GtfsMutableRelationalDao dao;
    private final TransformContext transformContext;
    private final PathwaysData pd;
    private final Multimap<Pair<Integer, String>, PlatformStopMapping> platformStopMapping;
    private final Map<String, ReferenceStation> stations;
    private final Map<Integer, ReferenceStationComplex> stationComplexes;
    private final Map<Integer, String> stationComplexNames;
    private final Map<Integer, Point> stationComplexCentroids;
    private final Map<String, Equipment> equipment;

    private final AtomicInteger pathwayCount = new AtomicInteger();

    public void run() {
        for (StationComplex stationComplex : pd.getStationComplexes()) {
            final int stationComplexId = stationComplex.getStationComplexId();
            final Point stationComplexCentroid = stationComplexCentroids.get(stationComplexId);

            final Stop sc = new Stop();
            sc.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), String.format("MR%03d", stationComplexId)));
            sc.setName(stationComplexNames.get(stationComplexId));
            sc.setLat(stationComplexCentroid.getY());
            sc.setLon(stationComplexCentroid.getX());
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
                try {
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
                } catch (Exception e) {
                    log.error("Exception while processing connection: " + connection.toString(), e);
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
                .flatMap(stopId -> dao.getAllTransfers().stream()
                        .filter(t -> t.getFromStop().getId().getId().equals(stopId) || t.getToStop().getId().getId().equals(stopId))
                )
                .collect(toImmutableSet());

        transfersToDelete.forEach(dao::removeEntity);
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
            p.setReversedSignpostedAs(connection.getSignpostedAsFrom());
            p.setSignpostedAs(connection.getSignpostedAsTo());
        } else {
            p.setSignpostedAs(connection.getSignpostedAsFrom());
            p.setReversedSignpostedAs(connection.getSignpostedAsTo());
        }

        final MtaEquipmentIdExtension equipmentId = new MtaEquipmentIdExtension();
        final IsAccessibleExtension isAccessible = new IsAccessibleExtension();

        final PathwayType pt = connection.getPathwayType();

        if (pt == STAIR) {
            Stream<Stair> stairs = stairsComplexAndId(connection).map(id -> pd.getStairsByComplexAndId().get(id)).filter(Objects::nonNull);
            equipmentId.setMtaEquipmentId(stairs.map(Stair::getNyctEamStairId).collect(Collectors.joining(";")));
            isAccessible.setIsAccessible(IS_NOT_ACCESSIBLE);
        } else if (pt == WALKWAY) {
            Walkway w = pd.getWalkwaysByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(w.getNyctWalkwayId());
            isAccessible.setIsAccessible(IS_ACCESSIBLE);
        } else if (pt == ELEVATOR) {
            Elevator e = pd.getElevatorsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(e.getNyctElevatorId());
            if (equipment != null && equipment.containsKey(e.getNyctElevatorId())) {
                equipmentId.setMtaEquipmentDescription(cleanEmbeddedNewlines(equipment.get(e.getNyctElevatorId()).getServing()));
            }
            isAccessible.setIsAccessible(IS_ACCESSIBLE);
        } else if (pt == ESCALATOR) {
            Escalator e = pd.getEscalatorsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(e.getNyctEscalatorId());
            if (equipment != null && equipment.containsKey(e.getNyctEscalatorId())) {
                equipmentId.setMtaEquipmentDescription(cleanEmbeddedNewlines(equipment.get(e.getNyctEscalatorId()).getServing()));
            }
            isAccessible.setIsAccessible(IS_NOT_ACCESSIBLE);
        } else if (pt == FARE_CONTROL) {
            FareControlArea fca = pd.getFareControlAreasByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(fca.getNyctFareControlAreaId());
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
                connection.getPathwayId()
        );
    }

    @NotNull
    private static Stream<Pair<Integer, Integer>> stairsComplexAndId(@NotNull Connection connection) {
        return Stream.of(
                connection.getPathwayId(),
                connection.getStair2Id(),
                connection.getStair3Id(),
                connection.getStair4Id())
                .filter(id -> id != 0)
                .map(id -> ImmutablePair.of(connection.getStationComplexId(), id));
    }

    private static String cleanEmbeddedNewlines(String input) {
        return Pattern.compile("\\R+").matcher(input).replaceAll(" ");
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
        return Optional.ofNullable(dao.getStopForId(id))
                .orElseThrow(() -> new RuntimeException("Failed to get stop with ID: " + id.toString()));
    }

    private Stream<Stop> gtfsStopForNode(int stationComplexId, Pair<NodeType, Integer> nodeTypeAndId, Stop stationComplex) {
        NodeType nt = nodeTypeAndId.getLeft();
        int nodeId = nodeTypeAndId.getRight();

        return switch (nt) {
            case ENTRANCE -> {
                final Entrance e = pd.getEntrancesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

                if (e == null) {
                    log.warn("Cannot find entrance {} at station complex {}", nodeId, stationComplexId);
                    yield Stream.empty();
                }

                final Stop s = new Stop();

                s.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), idForNode(stationComplexId, nt, nodeId)));
                s.setLocationType(Stop.LOCATION_TYPE_ENTRANCE_EXIT);
                s.setLat(e.getLatitude());
                s.setLon(e.getLongitude());
                s.setName(e.getDescription());
                s.setParentStation(stationComplex.getId().getId());

                yield Stream.of(s);

            }
            case PLATFORM -> {
                final Platform p = pd.getPlatformsByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

                if (p == null) {
                    log.warn("Cannot find platform {} at station complex {}", nodeId, stationComplexId);
                    yield Stream.empty();
                }

                final Collection<PlatformStopMapping> platformStopMappings = platformStopMapping.get(ImmutablePair.of(p.getStationComplexId(), p.getNyctPlatformId()));

                if (platformStopMappings.isEmpty()) {
                    log.warn("No stop mappings found for platform {} at station complex {}", nodeId, stationComplexId);
                }

                yield platformStopMappings.stream()
                        .map(PlatformStopMapping::getGtfsStopId)
                        .map(stopId -> new AgencyAndId(transformContext.getDefaultAgencyId(), stopId))
                        .map(this::getStop);
            }
            case MEZZANINE -> {
                final Mezzanine m = pd.getMezzaninesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

                if (m == null) {
                    log.warn("Cannot find mezzanine {} at station complex {}", nodeId, stationComplexId);
                    yield Stream.empty();
                }

                final Stop s = new Stop();

                s.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), idForNode(stationComplexId, nt, nodeId)));
                s.setLocationType(Stop.LOCATION_TYPE_NODE);
                s.setName(m.getDescription());
                s.setParentStation(stationComplex.getId().getId());

                yield Stream.of(s);
            }
        };
    }
}
