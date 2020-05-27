package com.nyct.dos.tpc.pathways.model.referencedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceStation {
    @JsonProperty("Station ID")
    int stationId;

    @JsonProperty("Complex ID")
    int stationComplexId;

    @JsonProperty("GTFS Stop ID")
    String gtfsStopId;

    @JsonProperty("Stop Name")
    String stopName;
}
