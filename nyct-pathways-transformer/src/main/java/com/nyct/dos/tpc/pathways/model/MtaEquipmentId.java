package com.nyct.dos.tpc.pathways.model;

import lombok.Data;
import org.onebusaway.csv_entities.schema.annotations.CsvField;

@Data
public class MtaEquipmentId {
    @CsvField(optional = true)
    private String mtaEquipmentId;
}
