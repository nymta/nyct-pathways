package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Stair {
    public static final String FILENAME = "STAIR_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("STAIR_00")
    private int stairId;

    @JsonProperty("STAIR_ID")
    private String nyctEamStairId;

}
