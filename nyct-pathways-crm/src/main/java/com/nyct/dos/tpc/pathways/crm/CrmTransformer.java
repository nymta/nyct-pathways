package com.nyct.dos.tpc.pathways.crm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.*;
import com.nyct.dos.tpc.pathways.crm.model.*;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.model.StationComplex;
import com.nyct.dos.tpc.pathways.model.nodes.Platform;
import com.nyct.dos.tpc.pathways.model.referencedata.PlatformStopMapping;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import com.nyct.dos.tpc.pathways.types.NycBorough;
import com.nyct.dos.tpc.pathways.types.ServiceDirection;
import com.nyct.dos.tpc.pathways.types.ServiceType;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Point;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.MoreCollectors.onlyElement;
import static com.nyct.dos.tpc.pathways.types.ServiceDirection.*;
import static com.nyct.dos.tpc.pathways.types.ServiceType.*;

@RequiredArgsConstructor
public class CrmTransformer {

    private final PathwaysData pd;
    private final Multimap<Pair<Integer, String>, PlatformStopMapping> platformStopMapping;
    private final Map<String, ReferenceStation> referenceStations;
    private final Map<Integer, ReferenceStationComplex> referenceStationComplexes;
    private final Map<Integer, String> stationComplexNames;
    private final Map<Integer, Point> stationComplexCentroids;
    private final Path outputPath;

    private NycBorough boroughForStationComplex(int stationComplexId) {
        return referenceStations.values()
                .stream()
                .filter(rs -> rs.getStationComplexId() == stationComplexId)
                .map(ReferenceStation::getBorough)
                .distinct()
                .collect(onlyElement());

    }

    private List<String> linesForStationComplex(int stationComplexId) {
        return referenceStations.values()
                .stream()
                .filter(rs -> rs.getStationComplexId() == stationComplexId)
                .map(ReferenceStation::getDaytimeRoutes)
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .distinct()
                .sorted()
                .collect(toImmutableList());
    }

    private String nameForBorough(NycBorough borough) {
        return switch (borough) {
            case MANHATTAN -> "Manhattan";
            case BRONX -> "The Bronx";
            case BROOKLYN -> "Brooklyn";
            case QUEENS -> "Queens";
            case STATEN_ISLAND -> "Staten Island";
        };
    }

    private List<CrmLine> buildLines() {
        Map<String, ListMultimap<NycBorough, CrmStation>> stationsByBoroughAndLine = new HashMap<>();

        for (StationComplex sc : pd.getStationComplexes()) {
            int stationComplexId = sc.getStationComplexId();

            CrmStation crmStation = new CrmStation(stationComplexId, stationComplexNames.get(stationComplexId));
            NycBorough borough = boroughForStationComplex(stationComplexId);
            List<String> lines = linesForStationComplex(stationComplexId);

            for (String line : lines) {
                stationsByBoroughAndLine.computeIfAbsent(line, k -> ArrayListMultimap.create()).put(borough, crmStation);
            }
        }

        List<CrmLine> crmLines = stationsByBoroughAndLine.entrySet().stream()
                .map(e1 -> {
                    String line = e1.getKey();
                    ListMultimap<NycBorough, CrmStation> stationsByBorough = e1.getValue();
                    ImmutableList<CrmBorough> boroughs = stationsByBorough.asMap().entrySet().stream()
                            .map(e2 -> {
                                NycBorough borough = e2.getKey();
                                List<CrmStation> stations = ImmutableList.copyOf(e2.getValue());

                                return new CrmBorough(nameForBorough(borough), stations);
                            })
                            .collect(toImmutableList());

                    return new CrmLine(line, boroughs);
                }).collect(toImmutableList());

        return crmLines;
    }

    private ServiceDirection directionForPlatform(Platform plat) {
        EnumSet<ServiceDirection> serviceDirections = Stream.of(plat.getServiceDirection1(), plat.getServiceDirection2())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ServiceDirection.class)));

        if (serviceDirections.containsAll(EnumSet.of(NORTHBOUND, SOUTHBOUND))) {
            serviceDirections.remove(NORTHBOUND);
            serviceDirections.remove(SOUTHBOUND);
            serviceDirections.add(BOTH);
        }

        if (serviceDirections.contains(BOTH)) {
            serviceDirections.remove(NORTHBOUND);
            serviceDirections.remove(SOUTHBOUND);
        }

        if (serviceDirections.isEmpty()) {
            serviceDirections.add(BOTH);
        }

        return Iterables.getOnlyElement(serviceDirections);
    }

    private String directionLabelForPlatform(Platform plat) {
        return switch (directionForPlatform(plat)) {
            case NORTHBOUND -> "Northbound";
            case SOUTHBOUND -> "Southbound";
            case BOTH -> "Both directions";
        };
    }

    private String serviceForPlatform(Platform plat) {
        EnumSet<ServiceType> serviceTypes = Stream.of(plat.getServiceType1(), plat.getServiceType2())
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(ServiceType.class)));


        if (serviceTypes.containsAll(EnumSet.of(EXPRESS, LOCAL))) {
            serviceTypes.remove(EXPRESS);
            serviceTypes.remove(LOCAL);
            serviceTypes.add(ALL);
        }

        if (serviceTypes.contains(ALL)) {
            serviceTypes.remove(EXPRESS);
            serviceTypes.remove(LOCAL);
        }

        if (serviceTypes.isEmpty()) {
            serviceTypes.add(ALL);
        }

        return switch (Iterables.getOnlyElement(serviceTypes)) {
            case EXPRESS -> "Express";
            case LOCAL -> "Local";
            case ALL -> "All service";
        };
    }

    private String destinationNameForPlatform(Platform plat) {
        Collection<PlatformStopMapping> platformStopMappings = platformStopMapping.get(ImmutablePair.of(plat.getStationComplexId(), plat.getNyctPlatformId()));

        return platformStopMappings.stream()
                .flatMap(psm -> {
                    ReferenceStation rs = referenceStations.get(psm.getGtfsParentStopId());
                    return switch (psm.getDirection()) {
                        case NORTHBOUND -> Stream.of(rs.getNorthDirectionLabel());
                        case SOUTHBOUND -> Stream.of(rs.getSouthDirectionLabel());
                        case BOTH -> Stream.of(rs.getNorthDirectionLabel(), rs.getSouthDirectionLabel());
                    };
                })
                .filter(Objects::nonNull)
                .filter(s -> !s.isBlank())
                .distinct()
                .collect(Collectors.joining(", "));
    }

    private String nameForPlatform(Platform plat) {
        String lines = Arrays.stream(plat.getLines().split(" "))
                .map(s -> s.replace("(", "").replace(")", "").replace("x", ""))
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
        ServiceDirection direction = directionForPlatform(plat);
        String destinationName = destinationNameForPlatform(plat);

        String separator = destinationName.startsWith("Uptown") || destinationName.startsWith("Downtown") ? " " : " to ";

        if (destinationName.isBlank()) {
            return "Train terminates here";
        } else {
            return switch (direction) {
                case NORTHBOUND, SOUTHBOUND -> lines + separator + destinationName;
                case BOTH -> lines + " platform";
            };
        }
    }

    private List<CrmStationDetail> buildStationDetails() {
        List<CrmStationDetail> crmStationDetails = pd.getStationComplexes().stream().map(sc -> {
            int stationComplexId = sc.getStationComplexId();

            NycBorough borough = boroughForStationComplex(stationComplexId);
            Point stationComplexCentroid = stationComplexCentroids.get(stationComplexId);

            List<CrmPlatform> platforms = pd.getPlatformsByComplexAndId().entrySet().stream()
                    .filter(e -> e.getKey().getLeft() == stationComplexId)
                    .map(Entry::getValue)
                    .map(plat -> new CrmPlatform(
                            plat.getNyctPlatformId(),
                            plat.getLines(),
                            directionLabelForPlatform(plat),
                            serviceForPlatform(plat),
                            destinationNameForPlatform(plat),
                            nameForPlatform(plat)
                    ))
                    .collect(toImmutableList());

            List<CrmEntrance> entrances = pd.getEntrancesByComplexAndId().entrySet().stream()
                    .filter(e -> e.getKey().getLeft() == stationComplexId)
                    .map(Entry::getValue)
                    .map(entrance -> new CrmEntrance(
                            Integer.toString(entrance.getEntranceId()),
                            entrance.getLatitude(),
                            entrance.getLongitude(),
                            entrance.getDescription()
                    ))
                    .collect(toImmutableList());

            return new CrmStationDetail(
                    stationComplexId,
                    stationComplexNames.get(stationComplexId),
                    nameForBorough(borough),
                    stationComplexCentroid.getY(),
                    stationComplexCentroid.getX(),
                    platforms,
                    entrances
            );
        }).collect(toImmutableList());

        return crmStationDetails;
    }

    public void run() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        ObjectWriter objectWriter = mapper.writerWithDefaultPrettyPrinter();

        objectWriter.writeValue(
                outputPath.resolve("Obj-line-station.json").toFile(),
                ImmutableMap.of(
                        "stations", buildStationDetails(),
                        "lines", buildLines()
                )
        );

    }

}
