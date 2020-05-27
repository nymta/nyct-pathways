package com.nyct.dos.tpc.pathways.model;

import com.google.common.collect.ListMultimap;
import com.nyct.dos.tpc.pathways.model.edges.*;
import com.nyct.dos.tpc.pathways.model.nodes.Entrance;
import com.nyct.dos.tpc.pathways.model.nodes.Mezzanine;
import com.nyct.dos.tpc.pathways.model.nodes.Platform;
import lombok.Data;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

@Data
public class PathwaysData {
    private final List<StationComplex> stationComplexes;
    private final ListMultimap<Integer, Connection> connectionsByComplex;

    private final Map<Pair<Integer, Integer>, Elevator> elevatorsByComplexAndId;
    private final Map<Pair<Integer, Integer>, Escalator> escalatorsByComplexAndId;
    private final Map<Pair<Integer, Integer>, Stair> stairsByComplexAndId;
    private final Map<Pair<Integer, Integer>, Walkway> walkwaysByComplexAndId;
    private final Map<Pair<Integer, Integer>, FareControlArea> fareControlAreasByComplexAndId;

    private final Map<Pair<Integer, Integer>, Entrance> entrancesByComplexAndId;
    private final Map<Pair<Integer, Integer>, Mezzanine> mezzaninesByComplexAndId;
    private final Map<Pair<Integer, Integer>, Platform> platformsByComplexAndId;
}
