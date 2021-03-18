package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NycBorough {
    @JsonProperty("M")
    MANHATTAN,
    @JsonProperty("Bx")
    BRONX,
    @JsonProperty("Bk")
    BROOKLYN,
    @JsonProperty("Q")
    QUEENS,
    @JsonProperty("SI")
    STATEN_ISLAND
}
