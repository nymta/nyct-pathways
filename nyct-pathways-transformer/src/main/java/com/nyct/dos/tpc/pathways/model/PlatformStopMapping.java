package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties({"MRN", "StationName", "GTFS", "Direction"})
public class PlatformStopMapping {

    @JsonProperty("Complex")
    private int stationComplexId;

    @JsonProperty("Platform")
    private String platformId;

    @JsonProperty("GTFS.Stop.ID")
    private String gtfsStopId;

}
