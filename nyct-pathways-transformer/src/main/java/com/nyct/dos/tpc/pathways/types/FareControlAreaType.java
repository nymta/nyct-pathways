package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum FareControlAreaType {

    @JsonProperty("1")
    LOW_TURNSTILE,
    @JsonProperty("2")
    HEET,
    @JsonProperty("3")
    HXT
}
