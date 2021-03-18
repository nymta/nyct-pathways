package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ServiceDirection {
    @JsonProperty("NB")
    NORTHBOUND,
    @JsonProperty("SB")
    SOUTHBOUND,
    @JsonProperty("NB&SB")
    BOTH
}
