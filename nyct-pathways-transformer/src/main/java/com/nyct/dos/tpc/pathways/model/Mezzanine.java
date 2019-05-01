package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Mezzanine {

    public static final String FILENAME = "MEZZANINE_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("M00")
    private int mezzanineId;

    @JsonProperty("MEZZ_ID")
    private String description;

}
