package com.nyct.dos.tpc.pathways.model.referencedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyct.dos.tpc.pathways.types.NycBorough;
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

    @JsonProperty("Borough")
    NycBorough borough;

    @JsonProperty("Daytime Routes")
    String daytimeRoutes;

    @JsonProperty("GTFS Latitude")
    double gtfsLatitude;

    @JsonProperty("GTFS Longitude")
    double gtfsLongitude;

    @JsonProperty("North Direction Label")
    String northDirectionLabel;
    @JsonProperty("South Direction Label")
    String southDirectionLabel;
}
