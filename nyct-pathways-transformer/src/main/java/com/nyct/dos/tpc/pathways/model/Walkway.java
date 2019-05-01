package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Walkway {

    public static final String FILENAME = "WALKWAY_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("WALKWAY_ID")
    private String walkwayId;

    @JsonProperty("WALKWAY_DESCRIPTION")
    private String description;

    @JsonProperty("WR_ACCESSIBLE")
    private int accessible;

}
