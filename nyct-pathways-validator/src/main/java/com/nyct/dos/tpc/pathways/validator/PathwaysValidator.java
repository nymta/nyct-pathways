package com.nyct.dos.tpc.pathways.validator;

import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.types.NodeType;
import com.nyct.dos.tpc.pathways.types.PathwayType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Map;

@Slf4j
@RequiredArgsConstructor
public class PathwaysValidator {

    private final PathwaysData pd;

    private void validateNode(int stationComplexId, NodeType nt, int nodeId) {
        final Object nodeObject = switch (nt) {
            case ENTRANCE -> pd.getEntrancesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));
            case PLATFORM -> pd.getPlatformsByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));
            case MEZZANINE -> pd.getMezzaninesByComplexAndId().get(ImmutablePair.of(stationComplexId, nodeId));
        };

        if (nodeObject == null) {
            log.warn("Cannot find {} {} at station complex {}", nt, nodeId, stationComplexId);
        }
    }

    private void validateEdge(int stationComplexId, PathwayType pt, int pathwayId) {
        final Object edgeObject = switch (pt) {
            case STAIR -> pd.getStairsByComplexAndId().get(ImmutablePair.of(stationComplexId, pathwayId));
            case WALKWAY -> pd.getWalkwaysByComplexAndId().get(ImmutablePair.of(stationComplexId, pathwayId));
            case ELEVATOR -> pd.getElevatorsByComplexAndId().get(ImmutablePair.of(stationComplexId, pathwayId));
            case ESCALATOR -> pd.getEscalatorsByComplexAndId().get(ImmutablePair.of(stationComplexId, pathwayId));
            case FARE_CONTROL -> pd.getFareControlAreasByComplexAndId().get(ImmutablePair.of(stationComplexId, pathwayId));
        };

        if (edgeObject == null) {
            log.warn("Cannot find {} {} at station complex {}", pt, pathwayId, stationComplexId);
        }
    }

    void validate() {
        pd.getStationComplexes().forEach(sc -> {
            System.out.printf("Station Complex: %03d\t%s%n%s%n%n", sc.getStationComplexId(), sc.getStationComplexName(),
                    formatCounts(
                            getCountForStationComplex(pd.getEntrancesByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getPlatformsByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getMezzaninesByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getStairsByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getWalkwaysByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getElevatorsByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getEscalatorsByComplexAndId(), sc.getStationComplexId()),
                            getCountForStationComplex(pd.getFareControlAreasByComplexAndId(), sc.getStationComplexId())
                    )
            );

            pd.getConnectionsByComplex().get(sc.getStationComplexId()).forEach(c -> {
                validateNode(c.getStationComplexId(), c.getConnectFromType(), c.getConnectFromId());
                validateNode(c.getStationComplexId(), c.getConnectToType(), c.getConnectToId());


                validateEdge(c.getStationComplexId(), c.getPathwayType(), c.getPathwayId());

                if (c.getPathwayType() == PathwayType.STAIR) {
                    if (c.getStair2Id() != 0) {
                        validateEdge(c.getStationComplexId(), c.getPathwayType(), c.getStair2Id());
                    }
                    if (c.getStair3Id() != 0) {
                        validateEdge(c.getStationComplexId(), c.getPathwayType(), c.getStair3Id());
                    }
                    if (c.getStair4Id() != 0) {
                        validateEdge(c.getStationComplexId(), c.getPathwayType(), c.getStair4Id());
                    }
                }
            });
        });

        int stationComplexCount = pd.getStationComplexes().size();
        int connectionCount = pd.getConnectionsByComplex().size();

        int entranceCount = pd.getEntrancesByComplexAndId().size();
        int platformCount = pd.getPlatformsByComplexAndId().size();
        int mezzanineCount = pd.getMezzaninesByComplexAndId().size();

        int stairCount = pd.getStairsByComplexAndId().size();
        int walkwayCount = pd.getWalkwaysByComplexAndId().size();
        int elevatorCount = pd.getElevatorsByComplexAndId().size();
        int escalatorCount = pd.getEscalatorsByComplexAndId().size();
        int fareControlCount = pd.getFareControlAreasByComplexAndId().size();

        System.out.printf("Station Complexes: %03d\tConnections: %05d%n%s%n",
                stationComplexCount, connectionCount,
                formatCounts(entranceCount, platformCount, mezzanineCount,
                        stairCount, walkwayCount, elevatorCount, escalatorCount, fareControlCount));
    }

    private static long getCountForStationComplex(Map<Pair<Integer, Integer>, ?> entityMap, int stationComplexId) {
        return entityMap.keySet().stream().filter(p -> p.getLeft().equals(stationComplexId)).count();
    }

    private static String formatCounts(long entranceCount, long platformCount, long mezzanineCount, long stairCount,
                                       long walkwayCount, long elevatorCount, long escalatorCount,
                                       long fareControlCount) {
        return String.format("Entrances: %05d\tPlatforms: %05d\tMezzanines: %05d\nStairs: %05d\tWalkways: %05d\tElevators: %05d\tEscalators: %05d\tFare Control Areas: %05d",
                entranceCount, platformCount, mezzanineCount,
                stairCount, walkwayCount, elevatorCount, escalatorCount, fareControlCount);
    }
}
