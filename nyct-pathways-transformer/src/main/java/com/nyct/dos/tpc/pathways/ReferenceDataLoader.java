package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Multimap;
import com.google.common.collect.Streams;
import com.nyct.dos.tpc.pathways.model.referencedata.PlatformStopMapping;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;
import static java.util.stream.Collectors.*;
import static org.locationtech.jts.geom.PrecisionModel.FLOATING;

@UtilityClass
@SuppressWarnings("UnstableApiUsage")
public class ReferenceDataLoader {

    private final static CsvSchema BOOTSTRAP_SCHEMA = CsvSchema.emptySchema()
            .withHeader();
    private final static ObjectMapper MAPPER = new CsvMapper();
    private final static GeometryFactory GEOMETRY_FACTORY = new GeometryFactory(new PrecisionModel(FLOATING), 4326);

    public static Multimap<Pair<Integer, String>, PlatformStopMapping> loadPlatformStopMapping(File platformStopMappingFile) throws IOException {
        final MappingIterator<PlatformStopMapping> iterator = MAPPER.readerFor(PlatformStopMapping.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(platformStopMappingFile);

        return Streams.stream(iterator)
                .collect(
                        toImmutableSetMultimap(
                                psm -> ImmutablePair.of(psm.getStationComplexId(), psm.getPlatformId()),
                                Function.identity()
                        )
                );
    }

    public static Map<String, ReferenceStation> loadStationsFile(File stationsFile) throws IOException {
        final MappingIterator<ReferenceStation> iterator = MAPPER.readerFor(ReferenceStation.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(stationsFile);

        return Streams.stream(iterator)
                .collect(toImmutableMap(ReferenceStation::getGtfsStopId, Function.identity()));
    }

    public static Map<Integer, ReferenceStationComplex> loadStationComplexesFile(File stationComplexesFile) throws IOException {
        final MappingIterator<ReferenceStationComplex> iterator = MAPPER.readerFor(ReferenceStationComplex.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(stationComplexesFile);

        return Streams.stream(iterator)
                .collect(toImmutableMap(ReferenceStationComplex::getStationComplexId, Function.identity()));
    }

    public static Map<Integer, String> buildStationComplexNames(Collection<ReferenceStation> stations, Collection<ReferenceStationComplex> stationComplexes) {
        return Stream.concat(
                stations
                        .stream()
                        .filter(s -> s.getStationId() == s.getStationComplexId())
                        .map(s -> Pair.of(s.getStationId(), s.getStopName())),

                stationComplexes
                        .stream()
                        .map(sc -> Pair.of(sc.getStationComplexId(), sc.getStationComplexName()))
        )
                .distinct()
                .collect(toImmutableMap(Pair::getKey, Pair::getValue));
    }

    public static Map<Integer, Point> buildStationComplexCentroids(Collection<ReferenceStation> stations, Collection<ReferenceStationComplex> stationComplexes) {
        return stations.stream()
                .collect(groupingBy(ReferenceStation::getStationComplexId,
                        mapping(
                                s -> GEOMETRY_FACTORY.createPoint(new CoordinateXY(s.getGtfsLongitude(), s.getGtfsLatitude())),
                                collectingAndThen(
                                        toImmutableList(),
                                        l -> GEOMETRY_FACTORY.createMultiPoint(l.toArray(Point[]::new)).getCentroid()
                                )
                        )
                ));
    }

}
