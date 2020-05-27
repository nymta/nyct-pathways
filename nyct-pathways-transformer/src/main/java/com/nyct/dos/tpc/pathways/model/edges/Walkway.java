package com.nyct.dos.tpc.pathways.model.edges;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Walkway {

    public static final String FILENAME = "WALKWAY_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("W00")
    Integer walkwayId;

    @JsonProperty("WALKWAY_ID")
    String nyctWalkwayId;

    @JsonProperty("WALKWAY_DESCRIPTION")
    String description;

    @JsonProperty("WR_ACCESSIBLE")
    int accessible;

}
