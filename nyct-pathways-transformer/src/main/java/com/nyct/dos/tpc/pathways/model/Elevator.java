package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Elevator {

    public static final String FILENAME = "ELEVATOR_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("ELEV_00")
    private int elevatorId;

    @JsonProperty("ELEVATOR_ID")
    private String nyctElevatorId;

}
