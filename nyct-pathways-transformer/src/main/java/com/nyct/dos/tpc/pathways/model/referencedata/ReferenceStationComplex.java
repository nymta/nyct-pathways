package com.nyct.dos.tpc.pathways.model.referencedata;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ReferenceStationComplex {
    @JsonProperty("Complex ID")
    int stationComplexId;

    @JsonProperty("Complex Name")
    String stationComplexName;

}
