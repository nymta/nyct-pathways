package com.nyct.dos.tpc.pathways.model.edges;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Elevator {

    public static final String FILENAME = "ELEVATOR_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("ELEV_00")
    int elevatorId;

    @JsonProperty("ELEVATOR_ID")
    String nyctElevatorId;

}
