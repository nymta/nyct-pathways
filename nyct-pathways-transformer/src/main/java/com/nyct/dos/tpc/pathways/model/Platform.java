package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties({"PLATFORM_TYPE", "TRACKS_SERVED_1", "PL_SERVICE_DIRECTION_1", "PL_SERVICE_TYPE_1",
        "TRACKS_SERVED_2", "PL_SERVICE_DIRECTION_2", "PL_SERVICE_TYPE_2", "PL_LINES"})
public class Platform {
    public static final String FILENAME = "PLATFORM_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("P00")
    private int platformId;

    @JsonProperty("PL_ID")
    private String nyctPlatformId;

}
