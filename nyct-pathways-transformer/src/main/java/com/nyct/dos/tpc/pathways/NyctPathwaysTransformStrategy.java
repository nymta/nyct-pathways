package com.nyct.dos.tpc.pathways;

import com.google.common.collect.Multimap;
import com.nyct.dos.tpc.pathways.model.PathwaysData;
import com.nyct.dos.tpc.pathways.model.eerms.Equipment;
import com.nyct.dos.tpc.pathways.model.extensions.IsAccessibleExtension;
import com.nyct.dos.tpc.pathways.model.extensions.MtaEquipmentIdExtension;
import com.nyct.dos.tpc.pathways.model.referencedata.PlatformStopMapping;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStation;
import com.nyct.dos.tpc.pathways.model.referencedata.ReferenceStationComplex;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.locationtech.jts.geom.Point;
import org.onebusaway.csv_entities.schema.DefaultEntitySchemaFactory;
import org.onebusaway.csv_entities.schema.annotations.CsvField;
import org.onebusaway.gtfs.model.Pathway;
import org.onebusaway.gtfs.services.GtfsMutableRelationalDao;
import org.onebusaway.gtfs_transformer.services.GtfsTransformStrategy;
import org.onebusaway.gtfs_transformer.services.TransformContext;

import java.io.File;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class NyctPathwaysTransformStrategy implements GtfsTransformStrategy {

    @Override
    public String getName() {
        return this.getClass().getName();
    }

    @Override
    public void updateWriteSchema(DefaultEntitySchemaFactory factory) {
        factory.addExtension(Pathway.class, MtaEquipmentIdExtension.class);
        factory.addExtension(Pathway.class, IsAccessibleExtension.class);
    }

    @Setter
    @CsvField
    private File basePath;

    @Setter
    @CsvField
    private File platformStopMappingFile;

    @Setter
    @CsvField
    private File stationComplexesFile;

    @Setter
    @CsvField
    private File stationsFile;

    @Setter
    @CsvField(optional = true)
    private File equipmentFile;

    public void run(TransformContext transformContext, GtfsMutableRelationalDao dao) {
        try {
            final PathwaysLoader pl = new PathwaysLoader(basePath);
            final PathwaysData pd = pl.load();

            final Multimap<Pair<Integer, String>, PlatformStopMapping> platformStopMapping = ReferenceDataLoader.loadPlatformStopMapping(platformStopMappingFile);
            final Map<String, ReferenceStation> stations = ReferenceDataLoader.loadStationsFile(stationsFile);
            final Map<Integer, ReferenceStationComplex> stationComplexes = ReferenceDataLoader.loadStationComplexesFile(stationComplexesFile);

            final Map<Integer, String> stationComplexNames = ReferenceDataLoader.buildStationComplexNames(stations.values(), stationComplexes.values());

            final Map<Integer, Point> stationComplexCentroids = ReferenceDataLoader.buildStationComplexCentroids(stations.values(), stationComplexes.values());

            final Map<String, Equipment> equipment = (equipmentFile != null) ? EquipmentLoader.loadEquipment(equipmentFile) : null;

            new PathwaysTransformer(dao, transformContext, pd, platformStopMapping, stations, stationComplexes, stationComplexNames, stationComplexCentroids, equipment).run();

        } catch (IOException e) {
            log.error("IOException in processing:", e);
        }
    }

}
