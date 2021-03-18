package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum EquipmentType {
    @JsonProperty("EL")
    ELEVATOR,
    @JsonProperty("ES")
    ESCALATOR,
    @JsonProperty("PW")
    POWER_WALK
}
