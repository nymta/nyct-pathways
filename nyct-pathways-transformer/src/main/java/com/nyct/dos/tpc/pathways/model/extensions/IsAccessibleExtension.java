package com.nyct.dos.tpc.pathways.model.extensions;

import lombok.Data;
import org.onebusaway.csv_entities.schema.annotations.CsvField;

@Data
public class IsAccessibleExtension {
    public static final int IS_ACCESSIBLE = 1;
    public static final int IS_NOT_ACCESSIBLE = 2;

    @CsvField(optional = true)
    int isAccessible;
}
