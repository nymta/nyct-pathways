package com.nyct.dos.tpc.pathways.model;

import com.google.common.collect.ListMultimap;
import lombok.AllArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
public class PathwaysData {
    private final List<StationComplex> stationComplexes;
    private final ListMultimap<Integer, Connection> connectionsByComplex;

    private final Map<Pair<Integer, Integer>, Elevator> elevatorsByComplexAndId;
    private final Map<Pair<Integer, Integer>, Escalator> escalatorsByComplexAndId;
    private final Map<Pair<Integer, Integer>, Stair> stairsByComplexAndId;
    private final Map<Pair<Integer, String>, Walkway> walkwaysByComplexAndId;
    private final Map<Pair<Integer, Integer>, FareControlArea> fareControlAreasByComplexAndId;

    private final Map<Pair<Integer, Integer>, Entrance> entrancesByComplexAndId;
    private final Map<Pair<Integer, Integer>, Mezzanine> mezzaninesByComplexAndId;
    private final Map<Pair<Integer, Integer>, Platform> platformsByComplexAndId;

    public List<StationComplex> getStationComplexes() {
        return stationComplexes;
    }

    public ListMultimap<Integer, Connection> getConnectionsByComplex() {
        return connectionsByComplex;
    }

    public Map<Pair<Integer, Integer>, Elevator> getElevatorsByComplexAndId() {
        return elevatorsByComplexAndId;
    }

    public Map<Pair<Integer, Integer>, Escalator> getEscalatorsByComplexAndId() {
        return escalatorsByComplexAndId;
    }

    public Map<Pair<Integer, Integer>, Stair> getStairsByComplexAndId() {
        return stairsByComplexAndId;
    }

    public Map<Pair<Integer, String>, Walkway> getWalkwaysByComplexAndId() {
        return walkwaysByComplexAndId;
    }

    public Map<Pair<Integer, Integer>, FareControlArea> getFareControlAreasByComplexAndId() {
        return fareControlAreasByComplexAndId;
    }

    public Map<Pair<Integer, Integer>, Entrance> getEntrancesByComplexAndId() {
        return entrancesByComplexAndId;
    }

    public Map<Pair<Integer, Integer>, Mezzanine> getMezzaninesByComplexAndId() {
        return mezzaninesByComplexAndId;
    }

    public Map<Pair<Integer, Integer>, Platform> getPlatformsByComplexAndId() {
        return platformsByComplexAndId;
    }
}
