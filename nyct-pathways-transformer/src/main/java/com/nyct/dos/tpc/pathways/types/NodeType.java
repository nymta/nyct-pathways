package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum NodeType {
    @JsonProperty("1")
    ENTRANCE,
    @JsonProperty("2")
    MEZZANINE,
    @JsonProperty("3")
    PLATFORM
}
