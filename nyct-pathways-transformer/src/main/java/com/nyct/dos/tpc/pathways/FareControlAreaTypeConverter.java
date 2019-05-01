package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdConverter;
import com.nyct.dos.tpc.pathways.types.FareControlAreaType;

import java.util.EnumSet;
import java.util.stream.Collectors;

public class FareControlAreaTypeConverter extends StdConverter<String, EnumSet<FareControlAreaType>> {

    private static final ObjectMapper om = new ObjectMapper();

    @Override
    public EnumSet<FareControlAreaType> convert(String value) {
        return value.trim().chars()
                .mapToObj(c -> String.valueOf((char) c))
                .map(c -> om.convertValue(c, FareControlAreaType.class))
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(FareControlAreaType.class)));
    }
}
