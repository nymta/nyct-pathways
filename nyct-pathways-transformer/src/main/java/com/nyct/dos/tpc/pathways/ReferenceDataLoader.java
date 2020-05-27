package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.MappingIterator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.csv.CsvMapper;
import com.fasterxml.jackson.dataformat.csv.CsvSchema;
import com.google.common.collect.Multimap;
import com.nyct.dos.tpc.pathways.model.referencedata.PlatformStopMapping;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.common.collect.ImmutableSetMultimap.toImmutableSetMultimap;

public class ReferenceDataLoader {

    private final static CsvSchema BOOTSTRAP_SCHEMA = CsvSchema.emptySchema()
            .withHeader();

    private final static ObjectMapper MAPPER = new CsvMapper();

    static Multimap<Pair<Integer, String>, String> loadPlatformStopMapping(File platformStopMappingFile) throws IOException {
        final MappingIterator<PlatformStopMapping> iterator = MAPPER.readerFor(PlatformStopMapping.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(platformStopMappingFile);

        return iterator.readAll()
                .stream()
                .collect(
                        toImmutableSetMultimap(
                                psm -> ImmutablePair.of(psm.getStationComplexId(), psm.getPlatformId()),
                                PlatformStopMapping::getGtfsStopId
                        )
                );
    }

    static Map<String, ReferenceStation> loadStationsFile(File stationsFile) throws IOException {
        final MappingIterator<ReferenceStation> iterator = MAPPER.readerFor(ReferenceStation.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(stationsFile);

        return iterator.readAll()
                .stream()
                .collect(toImmutableMap(ReferenceStation::getGtfsStopId, Function.identity()));
    }

    static Map<Integer, ReferenceStationComplex> loadStationComplexesFile(File stationComplexesFile) throws IOException {
        final MappingIterator<ReferenceStationComplex> iterator = MAPPER.readerFor(ReferenceStationComplex.class)
                .with(BOOTSTRAP_SCHEMA)
                .readValues(stationComplexesFile);

        return iterator.readAll()
                .stream()
                .collect(toImmutableMap(ReferenceStationComplex::getStationComplexId, Function.identity()));
    }

    static Map<Integer, String> buildStationComplexNames(Collection<ReferenceStation> stations, Collection<ReferenceStationComplex> stationComplexes) throws IOException {
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

}
