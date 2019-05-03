package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Iterables;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.SetMultimap;
import com.nyct.dos.tpc.pathways.model.*;
import com.nyct.dos.tpc.pathways.types.FareControlAreaType;
import com.nyct.dos.tpc.pathways.types.NodeType;
import com.nyct.dos.tpc.pathways.types.PathwayType;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.AgencyAndId;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.model.Stop;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableSetMultimap.flatteningToImmutableSetMultimap;
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
        factory.addExtension(Pathway.class, MtaEquipmentId.class);
    }

    @Setter
    @CsvField
    private File basePath;

    @Setter
    @CsvField
    private File platformStopMappingFile;

    @CsvField(ignore = true)
    private GtfsMutableRelationalDao dao;
    @CsvField(ignore = true)
    private PathwaysData pd;
    @CsvField(ignore = true)
    private ListMultimap<Pair<Integer, String>, String> psm;
    @CsvField(ignore = true)
    private final AtomicInteger pathwayCount = new AtomicInteger();


    public void run(TransformContext transformContext, GtfsMutableRelationalDao dao) {
        this.dao = dao;
        PathwaysLoader pl = new PathwaysLoader(basePath);

        try {
            pd = pl.load();

            psm = loadPlatformStopMapping(platformStopMappingFile);

            for (StationComplex stationComplex : pd.getStationComplexes()) {
                int stationComplexId = stationComplex.getStationComplexId();

                Stop sc = new Stop();
                sc.setId(new AgencyAndId(transformContext.getDefaultAgencyId(), String.format("MR%03d", stationComplexId)));
                sc.setName(stationComplex.getStationName().trim());
                sc.setLocationType(Stop.LOCATION_TYPE_STATION);
                dao.saveOrUpdateEntity(sc);

                final SetMultimap<Pair<NodeType, Integer>, Stop> stopsForNodes = pd.getConnectionsByComplex().get(stationComplexId)
                        .stream()
                        .flatMap(connection -> Stream.of(
                                ImmutablePair.of(connection.getConnectFromType(), connection.getConnectFromId()),
                                ImmutablePair.of(connection.getConnectToType(), connection.getConnectToId())
                        ))
                        .distinct()
                        .collect(flatteningToImmutableSetMultimap(
                                Function.identity(),
                                p -> this.gtfsStopForNode(stationComplexId, p, sc)
                        ));

                stopsForNodes.values().forEach(dao::saveOrUpdateEntity);

                for (Connection connection : pd.getConnectionsByComplex().get(stationComplexId)) {
                    final Set<Stop> fromStops = stopsForNodes.get(ImmutablePair.of(connection.getConnectFromType(), connection.getConnectFromId()));
                    final Set<Stop> toStops = stopsForNodes.get(ImmutablePair.of(connection.getConnectToType(), connection.getConnectToId()));

                    for (Stop fromStop : fromStops) {
                        for (Stop toStop : toStops) {

                            if (connection.getPathwayType() != FARE_CONTROL) {
                                createPathway(connection, fromStop, toStop, connection.getPathwayType().getGtfsMode(), false);
                            } else if (connection.getPathwayType() == FARE_CONTROL && !isFareControlExitOnly(connection)) {
                                //unpaid side is always the from!

                                createPathway(connection, fromStop, toStop, Pathway.MODE_FAREGATE, false);
                                createPathway(connection, toStop, fromStop, Pathway.MODE_EXIT_GATE, true);

                            } else if (connection.getPathwayType() == FARE_CONTROL && isFareControlExitOnly(connection)) {
                                createPathway(connection, fromStop, toStop, Pathway.MODE_EXIT_GATE, false);
                            } else {
                                throw new IllegalStateException(connection.toString());
                            }
                        }
                    }
                }
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    private void createPathway(Connection connection, Stop fromStop, Stop toStop, int pathwayMode, boolean swapFlag) {
        final Pathway p = new Pathway();

        p.setId(new AgencyAndId("MTA NYCT", String.format("%03d-%03d-%04d", connection.getStationComplexId(), connection.getConnectionId(), pathwayCount.incrementAndGet())));

        p.setPathwayMode(pathwayMode);

        p.setFromStop(fromStop);
        p.setToStop(toStop);

        if (!swapFlag) {
            p.setSignpostedAs(connection.getSignpostedAsFrom().trim());
            p.setReversedSignpostedAs(connection.getSignpostedAsTo().trim());
        } else {
            p.setReversedSignpostedAs(connection.getSignpostedAsFrom().trim());
            p.setSignpostedAs(connection.getSignpostedAsTo().trim());
        }

        MtaEquipmentId equipmentId = new MtaEquipmentId();

        final PathwayType pt = connection.getPathwayType();

        if (pt == STAIR) {
            Stair s = pd.getStairsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(s.getNyctEamStairId());
        } else if (pt == WALKWAY) {
            //Walkway w = pd.getWalkwaysByComplexAndId().get(walkwayComplexAndId(connection));
            //equipmentId.setMtaEquipmentId(w.getWalkwayId());
        } else if (pt == ELEVATOR) {
            Elevator e = pd.getElevatorsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(e.getNyctElevatorId());
        } else if (pt == ESCALATOR) {
            Escalator e = pd.getEscalatorsByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(e.getNyctEscalatorId());
        } else if (pt == FARE_CONTROL) {
            FareControlArea fca = pd.getFareControlAreasByComplexAndId().get(complexAndId(connection));
            equipmentId.setMtaEquipmentId(fca.getNyctFareControlAreaId());
        }

        p.putExtension(MtaEquipmentId.class, equipmentId);

        p.setIsBidirectional(connection.getPathwayType() != FARE_CONTROL ? 1 : 0);


        if (p.getIsBidirectional() == 0) {
            p.setReversedSignpostedAs(null);
        }

        dao.saveOrUpdateEntity(p);
    }

    private boolean isFareControlExitOnly(Connection connection) {
        assert connection.getPathwayType() == FARE_CONTROL;
        FareControlArea fca = pd.getFareControlAreasByComplexAndId().get(complexAndId(connection));

        return fca.getType().size() == 1 && Iterables.getOnlyElement(fca.getType()).equals(FareControlAreaType.HXT);
    }

    private static ImmutablePair<Integer, String> walkwayComplexAndId(Connection connection) {
        return ImmutablePair.of(connection.getStationComplexId(), "W" + connection.getPathwayId().trim());
    }

    private static ImmutablePair<Integer, Integer> complexAndId(Connection connection) {
        return ImmutablePair.of(connection.getStationComplexId(),
                Integer.valueOf(connection.getPathwayId().trim(), 10));
    }

    private static ListMultimap<Pair<Integer, String>, String> loadPlatformStopMapping(File platformStopMappingFile) throws IOException {
        final CsvSchema bootstrapSchema = CsvSchema.emptySchema()
                .withHeader();

        final ObjectMapper mapper = new CsvMapper();

        final MappingIterator<PlatformStopMapping> iterator = mapper.readerFor(PlatformStopMapping.class)
                .with(bootstrapSchema)
                .readValues(platformStopMappingFile);


        return iterator.readAll()
                .stream()
                .collect(toImmutableListMultimap(
                        psm -> ImmutablePair.of(psm.getStationComplexId(), psm.getPlatformId()),
                        PlatformStopMapping::getGtfsStopId
                ));
    }

    private static String idForNode(int stationComplexId, NodeType nt, int nodeId) {
        return String.format("%03d-%s-%03d", stationComplexId, nt.name(), nodeId);
    }

    private Stream<Stop> gtfsStopForNode(int stationComplexId, Pair<NodeType, Integer> nodeTypeAndId, Stop stationComplex) {
        NodeType nt = nodeTypeAndId.getLeft();
        int nodeId = nodeTypeAndId.getRight();

        if (nt == ENTRANCE) {

            final Entrance e = pd.getEntrancesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

            final Stop s = new Stop();

            s.setId(new AgencyAndId("MTA NYCT", idForNode(stationComplexId, nt, nodeId)));
            s.setLocationType(Stop.LOCATION_TYPE_ENTRANCE_EXIT);
            s.setLat(e.getLatitude());
            s.setLon(e.getLongitude());
            s.setName(e.getDescription());
            s.setParentStation(stationComplex.getId().getId());

            return Stream.of(s);

        } else if (nt == PLATFORM) {

            final Platform p = pd.getPlatformsByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));
            final List<String> gtfsStopIds = psm.get(ImmutablePair.of(p.getStationComplexId(), p.getNyctPlatformId()));

            return gtfsStopIds.stream()
                    .map(stopId -> new AgencyAndId("MTA NYCT", stopId))
                    .map(dao::getStopForId);

        } else if (nt == MEZZANINE) {

            final Mezzanine m = pd.getMezzaninesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));

            final Stop s = new Stop();

            s.setId(new AgencyAndId("MTA NYCT", idForNode(stationComplexId, nt, nodeId)));
            s.setLocationType(Stop.LOCATION_TYPE_NODE);
            s.setName(m.getDescription().trim());
            s.setParentStation(stationComplex.getId().getId());

            return Stream.of(s);

        } else {
            throw new IllegalArgumentException(nt.name());
        }

    }

}
