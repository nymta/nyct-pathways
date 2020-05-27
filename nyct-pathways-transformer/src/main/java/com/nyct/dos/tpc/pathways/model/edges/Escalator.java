package com.nyct.dos.tpc.pathways.model.edges;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties({"Dir", "mechnical_stair_count", "traversal_time", "is_bidirectional"})
public class Escalator {

    public static final String FILENAME = "ESCALATOR_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("ES00")
    int escalatorId;

    @JsonProperty("ESCALATOR_ID")
    String nyctEscalatorId;

}
