package com.nyct.dos.tpc.pathways.model.referencedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyct.dos.tpc.pathways.types.ServiceDirection;
import lombok.Data;

@Data
@JsonIgnoreProperties({"MRN", "StationName"})
public class PlatformStopMapping {

    @JsonProperty("Complex")
    int stationComplexId;

    @JsonProperty("Platform")
    String platformId;

    @JsonProperty("Direction")
    ServiceDirection direction;

    @JsonProperty("GTFS.Stop.ID")
    String gtfsStopId;

    @JsonProperty("GTFS")
    String gtfsParentStopId;
}
