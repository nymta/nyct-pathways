package com.nyct.dos.tpc.pathways.model.edges;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.nyct.dos.tpc.pathways.types.FareControlAreaType;
import com.nyct.dos.tpc.pathways.util.FareControlAreaTypeConverter;
import lombok.Data;

import java.util.EnumSet;

@Data
public class FareControlArea {

    public static final String FILENAME = "FARE_C_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("F00")
    int fareControlAreaId;

    @JsonProperty("FARE_ID")
    String nyctFareControlAreaId;

    @JsonProperty("FC_TYPE")
    @JsonDeserialize(converter = FareControlAreaTypeConverter.class)
    EnumSet<FareControlAreaType> type;

    @JsonProperty("FC_AUTOGATE")
    String autogate;

    @JsonProperty("FARE_DESCRIBE")
    String description;
}
