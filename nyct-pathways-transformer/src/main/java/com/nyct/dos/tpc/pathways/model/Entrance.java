package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Entrance {

    public static final String FILENAME = "ENTRANCE_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("ENTRANCE_ID")
    private int entranceId;

    @JsonProperty("ENTRANCE_DESCRIBE")
    private String description;

    @JsonProperty("LATITUDE")
    private double latitude;

    @JsonProperty("LONGITUDE")
    private double longitude;


}
