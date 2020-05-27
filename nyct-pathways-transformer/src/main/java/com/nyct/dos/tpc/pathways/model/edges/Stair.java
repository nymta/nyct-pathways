package com.nyct.dos.tpc.pathways.model.edges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties({"stair_count"})
public class Stair {
    public static final String FILENAME = "STAIR_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("STAIR_00")
    int stairId;

    @JsonProperty("STAIR_ID")
    String nyctEamStairId;

}
