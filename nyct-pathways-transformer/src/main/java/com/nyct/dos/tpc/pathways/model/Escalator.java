package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Escalator {

    public static final String FILENAME = "ESCALATOR_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("ES00")
    private int escalatorId;

    @JsonProperty("ESCALATOR_ID")
    private String nyctEscalatorId;

}
