package com.nyct.dos.tpc.pathways.model.referencedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties({"MRN", "StationName", "GTFS", "Direction"})
public class PlatformStopMapping {

    @JsonProperty("Complex")
    int stationComplexId;

    @JsonProperty("Platform")
    String platformId;

    @JsonProperty("GTFS.Stop.ID")
    String gtfsStopId;

}
