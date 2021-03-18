package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyct.dos.tpc.pathways.types.DirectionalArrow;
import com.nyct.dos.tpc.pathways.types.NodeType;
import com.nyct.dos.tpc.pathways.types.PathwayType;
import lombok.Data;

@Data
@JsonIgnoreProperties(value = {"ADDITIONAL_STAIRCASES", "ADDITIONAL_STAIRCASES2", "ADDITIONAL_STAIRCASES3", "Dir",
        "mechnical_stair_count", "traversal_time", "is_bidirectional", "STAIR_ID_1", "S_COUNT_1",
        "STAIR_ID_2", "S_COUNT_2", "STAIR_ID_3", "S_COUNT_3", "STAIR_ID_4", "S_COUNT_4", "stair_count"})
public class Connection {

    public static final String FILENAME = "CONNECTIONS_REC.csv";

    @JsonProperty("STATION_ID")
    int stationComplexId;

    @JsonProperty("PATH_COUNTER")
    int connectionId;

    @JsonProperty("CONNECT_FROM_TYPE")
    NodeType connectFromType;
    @JsonProperty("CONNECT_TO_TYPE")
    NodeType connectToType;

    @JsonProperty("CONNECT_FROM_ID")
    int connectFromId;
    @JsonProperty("CONNECT_TO_ID")
    int connectToId;

    @JsonProperty("PATHWAY_TYPE_CONNECT")
    PathwayType pathwayType;

    @JsonProperty("PATHWAYID")
    int pathwayId;
    @JsonProperty("STAIR2ID")
    int stair2Id;
    @JsonProperty("STAIR3ID")
    int stair3Id;
    @JsonProperty("STAIR4ID")
    int stair4Id;

    @JsonProperty("SIGN_POSTED_AS_FROM")
    String signpostedAsFrom;
    @JsonProperty("SIGN_POSTED_AS_TO")
    String signpostedAsTo;

    @JsonProperty("LINE_BULLETS_FROM")
    String lineBulletsFrom;
    @JsonProperty("LINE_BULLETS_TO")
    String lineBulletsTo;

    @JsonProperty("DIRECTIONAL_ARROW_FROM")
    DirectionalArrow directionalArrowFrom;
    @JsonProperty("DIRECTIONAL_ARROW_TO")
    DirectionalArrow directionalArrowTo;

    @JsonProperty("ADDITIONAL_INSTRUCTIONS_FROM")
    String additionalInstructionsFrom;
    @JsonProperty("ADDITIONAL_INSTRUCTIONS_TO")
    String additionalInstructionsTo;

    @JsonProperty("ELEVATOR_BUTTON_FROM")
    String elevatorButtonFrom;
    @JsonProperty("ELEVATOR_BUTTON_TO")
    String elevatorButtonTo;

}
