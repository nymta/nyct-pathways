package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class StationComplex {

    public static final String FILENAME = "ID_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("STATION_NAME_VERIFY")
    private String stationName;

}
