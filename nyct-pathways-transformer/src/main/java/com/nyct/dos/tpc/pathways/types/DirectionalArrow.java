package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DirectionalArrow {
    @JsonProperty("0")
    NO_ARROW,
    @JsonProperty("1")
    UP,
    @JsonProperty("2")
    DOWN,
    @JsonProperty("3")
    LEFT,
    @JsonProperty("4")
    RIGHT,
    @JsonProperty("5")
    UP_LEFT,
    @JsonProperty("6")
    UP_RIGHT,
    @JsonProperty("7")
    DOWN_LEFT,
    @JsonProperty("8")
    DOWN_RIGHT
}
