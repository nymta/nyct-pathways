package com.nyct.dos.tpc.pathways.model.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Entrance {

    public static final String FILENAME = "ENTRANCE_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("ENTRANCE_ID")
    int entranceId;

    @JsonProperty("ENTRANCE_DESCRIBE")
    String description;

    @JsonProperty("LATITUDE")
    double latitude;

    @JsonProperty("LONGITUDE")
    double longitude;

}
