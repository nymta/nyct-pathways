package com.nyct.dos.tpc.pathways.model.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Mezzanine {

    public static final String FILENAME = "MEZZANINE_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("M00")
    int mezzanineId;

    @JsonProperty("MEZZ_ID")
    String description;

}
