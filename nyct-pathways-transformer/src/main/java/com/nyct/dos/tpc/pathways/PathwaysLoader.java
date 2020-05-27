package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.ListMultimap;
import com.nyct.dos.tpc.pathways.model.*;
import com.nyct.dos.tpc.pathways.model.edges.*;
import com.nyct.dos.tpc.pathways.model.nodes.Entrance;
import com.nyct.dos.tpc.pathways.model.nodes.Mezzanine;
import com.nyct.dos.tpc.pathways.model.nodes.Platform;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

class PathwaysLoader {
    private final File basePath;

    private static final CsvSchema bootstrapSchema = CsvSchema.emptySchema()
            .withHeader();

    private static final ObjectMapper mapper = new CsvMapper();

    static {
        mapper.configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);
    }

    PathwaysLoader(File basePath) {
        this.basePath = basePath;
    }

    private List<StationComplex> loadStationComplexes() throws IOException {
        MappingIterator<StationComplex> iterator = mapper.readerFor(StationComplex.class)
                .with(bootstrapSchema)
                .readValues(new File(basePath, StationComplex.FILENAME));

        List<StationComplex> stationComplexes =
                iterator.readAll()
                        .stream()
                        .collect(toImmutableList());

        return stationComplexes;
    }

    private ListMultimap<Integer, Connection> loadConnections() throws IOException {
        MappingIterator<Connection> iterator = mapper.readerFor(Connection.class)
                .with(bootstrapSchema)
                .readValues(new File(basePath, Connection.FILENAME));

        ListMultimap<Integer, Connection> connections =
                iterator.readAll()
                        .stream()
                        .filter(c -> c.getStationComplexId() != 0 && c.getConnectionId() != 0 && c.getPathwayType() != null)
                        .collect(toImmutableListMultimap(
                                Connection::getStationComplexId,
                                Function.identity()
                                )
                        );

        return connections;
    }

    private <T1, T2> Map<Pair<Integer, T1>, T2> loadEntities(Class<T2> cls, String filename,
                                                             Predicate<T2> entityFilter,
                                                             Function<T2, Pair<Integer, T1>> entityIdFunction) throws IOException {

        MappingIterator<T2> iterator = mapper.readerFor(cls)
                .with(bootstrapSchema)
                .readValues(new File(basePath, filename));

        Map<Pair<Integer, T1>, T2> loadedEntities =
                iterator.readAll()
                        .stream()
                        .filter(entityFilter)
                        .collect(toImmutableMap(
                                entityIdFunction,
                                Function.identity()
                                )
                        );

        return loadedEntities;
    }

    private Map<Pair<Integer, Integer>, Elevator> loadElevators() throws IOException {
        return loadEntities(Elevator.class, Elevator.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getElevatorId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getElevatorId()));
    }

    private Map<Pair<Integer, Integer>, Escalator> loadEscalators() throws IOException {
        return loadEntities(Escalator.class, Escalator.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getEscalatorId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getEscalatorId()));
    }

    private Map<Pair<Integer, Integer>, Stair> loadStairs() throws IOException {
        return loadEntities(Stair.class, Stair.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getStairId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getStairId()));
    }

    private Map<Pair<Integer, Integer>, Walkway> loadWalkways() throws IOException {
        return loadEntities(Walkway.class, Walkway.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getWalkwayId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getWalkwayId()));
    }

    private Map<Pair<Integer, Integer>, FareControlArea> loadFareControlAreas() throws IOException {
        return loadEntities(FareControlArea.class, FareControlArea.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getFareControlAreaId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getFareControlAreaId()));
    }

    private Map<Pair<Integer, Integer>, Entrance> loadEntrances() throws IOException {
        return loadEntities(Entrance.class, Entrance.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getEntranceId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getEntranceId()));
    }

    private Map<Pair<Integer, Integer>, Mezzanine> loadMezzanines() throws IOException {
        return loadEntities(Mezzanine.class, Mezzanine.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getMezzanineId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getMezzanineId()));
    }

    private Map<Pair<Integer, Integer>, Platform> loadPlatforms() throws IOException {
        return loadEntities(Platform.class, Platform.FILENAME,
                e -> e.getStationComplexId() != 0 && e.getPlatformId() != 0,
                e -> ImmutablePair.of(e.getStationComplexId(), e.getPlatformId()));
    }

    PathwaysData load() throws IOException {
        return new PathwaysData(
                loadStationComplexes(),
                loadConnections(),
                loadElevators(),
                loadEscalators(),
                loadStairs(),
                loadWalkways(),
                loadFareControlAreas(),
                loadEntrances(),
                loadMezzanines(),
                loadPlatforms()
        );
    }

}
