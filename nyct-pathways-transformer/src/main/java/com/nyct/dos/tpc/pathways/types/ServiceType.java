package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ServiceType {
    @JsonProperty("Express")
    EXPRESS,
    @JsonProperty("Local")
    LOCAL,
    ALL
}
