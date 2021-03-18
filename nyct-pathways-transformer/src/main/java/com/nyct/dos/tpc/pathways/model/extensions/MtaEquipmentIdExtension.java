package com.nyct.dos.tpc.pathways.model.extensions;

import lombok.Data;
import org.onebusaway.csv_entities.schema.annotations.CsvField;

@Data
public class MtaEquipmentIdExtension {
    @CsvField(optional = true)
    String mtaEquipmentId;

    @CsvField(optional = true)
    String mtaEquipmentDescription;
}
