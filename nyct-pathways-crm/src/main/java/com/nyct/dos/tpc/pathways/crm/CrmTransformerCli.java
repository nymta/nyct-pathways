package com.nyct.dos.tpc.pathways.crm;

import com.google.common.collect.Multimap;
import com.nyct.dos.tpc.pathways.PathwaysLoader;
import com.nyct.dos.tpc.pathways.ReferenceDataLoader;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.model.referencedata.PlatformStopMapping;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Point;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.Callable;

public class CrmTransformerCli implements Callable<Integer> {

    @Parameters(index = "0")
    private File basePath;

    @Parameters(index = "1")
    private File platformStopMappingFile;

    @Parameters(index = "2")
    private File stationsFile;

    @Parameters(index = "3")
    private File stationComplexesFile;

    @Parameters(index = "4")
    private Path outputPath;

    @Override
    public Integer call() throws IOException {
        final PathwaysLoader pl = new PathwaysLoader(basePath);
        final PathwaysData pd = pl.load();

        final Multimap<Pair<Integer, String>, PlatformStopMapping> platformStopMapping = ReferenceDataLoader.loadPlatformStopMapping(platformStopMappingFile);
        final Map<String, ReferenceStation> referenceStations = ReferenceDataLoader.loadStationsFile(stationsFile);
        final Map<Integer, ReferenceStationComplex> referenceStationComplexes = ReferenceDataLoader.loadStationComplexesFile(stationComplexesFile);

        final Map<Integer, String> stationComplexNames = ReferenceDataLoader.buildStationComplexNames(referenceStations.values(), referenceStationComplexes.values());

        final Map<Integer, Point> stationComplexCentroids = ReferenceDataLoader.buildStationComplexCentroids(referenceStations.values(), referenceStationComplexes.values());

        new CrmTransformer(pd, platformStopMapping, referenceStations, referenceStationComplexes, stationComplexNames, stationComplexCentroids, outputPath).run();

        return 0;
    }

    public static void main(String... args) {
        System.exit(new CommandLine(new CrmTransformerCli()).execute(args));
    }

}
