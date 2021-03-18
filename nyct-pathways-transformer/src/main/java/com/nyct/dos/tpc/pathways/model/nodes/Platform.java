package com.nyct.dos.tpc.pathways.model.nodes;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyct.dos.tpc.pathways.types.ServiceDirection;
import com.nyct.dos.tpc.pathways.types.ServiceType;
import lombok.Data;

@Data
@JsonIgnoreProperties({"PLATFORM_TYPE", "TRACKS_SERVED_1", "TRACKS_SERVED_2"})
public class Platform {

    public static final String FILENAME = "PLATFORM_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("P00")
    int platformId;

    @JsonProperty("PL_ID")
    String nyctPlatformId;

    @JsonProperty("PL_LINES")
    String lines;

    @JsonProperty("PL_SERVICE_DIRECTION_1")
    ServiceDirection serviceDirection1;
    @JsonProperty("PL_SERVICE_DIRECTION_2")
    ServiceDirection serviceDirection2;

    @JsonProperty("PL_SERVICE_TYPE_1")
    ServiceType serviceType1;
    @JsonProperty("PL_SERVICE_TYPE_2")
    ServiceType serviceType2;
}
