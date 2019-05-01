package com.nyct.dos.tpc.pathways.types;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.onebusaway.gtfs.model.Pathway;

public enum PathwayType {
    @JsonProperty("1")
    STAIR(Pathway.MODE_STAIRS),

    @JsonProperty("2")
    ELEVATOR(Pathway.MODE_ELEVATOR),

    @JsonProperty("3")
    ESCALATOR(Pathway.MODE_ESCALATOR),

    @JsonProperty("4")
    WALKWAY(Pathway.MODE_WALKWAY),

    @JsonProperty("5")
    FARE_CONTROL(Pathway.MODE_FAREGATE);

    private int gtfsMode;

    public int getGtfsMode() {
        return gtfsMode;
    }

    PathwayType(int gtfsMode) {
        this.gtfsMode = gtfsMode;
    }
}
