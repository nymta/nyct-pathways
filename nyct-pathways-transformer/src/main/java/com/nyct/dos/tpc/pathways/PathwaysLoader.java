package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.cfg.CoercionAction;
import com.fasterxml.jackson.databind.cfg.CoercionInputShape;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Streams;
import com.nyct.dos.tpc.pathways.model.Connection;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.model.StationComplex;
import com.nyct.dos.tpc.pathways.model.edges.*;
import com.nyct.dos.tpc.pathways.model.nodes.Entrance;
import com.nyct.dos.tpc.pathways.model.nodes.Mezzanine;
import com.nyct.dos.tpc.pathways.model.nodes.Platform;
import com.nyct.dos.tpc.pathways.util.TrimStringDeserializer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.google.common.collect.ImmutableListMultimap.toImmutableListMultimap;
import static com.google.common.collect.ImmutableMap.toImmutableMap;

@SuppressWarnings("UnstableApiUsage")
@Slf4j
@RequiredArgsConstructor
public
class PathwaysLoader {
    private final File basePath;

    private static final CsvSchema BOOTSTRAP_SCHEMA = CsvSchema.emptySchema()
            .withHeader();

    private static final ObjectMapper MAPPER = new CsvMapper();

    static {
        MAPPER.registerModule(
                new SimpleModule("TrimStringModule")
                        .addDeserializer(String.class, new TrimStringDeserializer()))
                .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true)
                .coercionConfigDefaults()
                .setAcceptBlankAsEmpty(true)
                .setCoercion(CoercionInputShape.EmptyString, CoercionAction.AsNull);
    }

    private List<StationComplex> loadStationComplexes() throws IOException {
        MappingIterator<StationComplex> iterator = MAPPER.readerFor(StationComplex.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(new File(basePath, StationComplex.FILENAME));

        List<StationComplex> stationComplexes = ImmutableList.copyOf(iterator);
        return stationComplexes;
    }

    private ListMultimap<Integer, Connection> loadConnections() throws IOException {
        MappingIterator<Connection> iterator = MAPPER.readerFor(Connection.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(new File(basePath, Connection.FILENAME));

        ListMultimap<Integer, Connection> connections =
                Streams.stream(iterator)
                        .filter(c -> c.getStationComplexId() != 0 && c.getConnectionId() != 0 && c.getPathwayType() != null)
                        .collect(
                                toImmutableListMultimap(
                                        Connection::getStationComplexId,
                                        Function.identity()
                                )
                        );

        return connections;
    }

    private <T2> Map<Pair<Integer, Integer>, T2> loadEntities(Class<T2> cls, String filename,
                                                              Predicate<T2> entityFilter,
                                                              Function<T2, Pair<Integer, Integer>> entityIdFunction) throws IOException {

        log.info("Loading {} from {}", cls.getName(), filename);

        MappingIterator<T2> iterator = MAPPER.readerFor(cls)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(new File(basePath, filename));

        Map<Pair<Integer, Integer>, T2> loadedEntities =
                Streams.stream(iterator)
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

    public PathwaysData load() throws IOException {
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
