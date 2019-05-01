package com.nyct.dos.tpc.pathways.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nyct.dos.tpc.pathways.types.DirectionalArrow;
import com.nyct.dos.tpc.pathways.types.NodeType;
import com.nyct.dos.tpc.pathways.types.PathwayType;
import lombok.Data;

@Data
@JsonIgnoreProperties({"ADDITIONAL_STAIRCASES", "ADDITIONAL_STAIRCASES2", "ADDITIONAL_STAIRCASES3"})
public class Connection {

    public static final String FILENAME = "CONNECTIONS_REC.TXT";

    @JsonProperty("STATION_ID")
    private int stationComplexId;

    @JsonProperty("PATH_COUNTER")
    private int connectionId;

    @JsonProperty("CONNECT_FROM_TYPE")
    private NodeType connectFromType;
    @JsonProperty("CONNECT_TO_TYPE")
    private NodeType connectToType;

    @JsonProperty("CONNECT_FROM_ID")
    private int connectFromId;
    @JsonProperty("CONNECT_TO_ID")
    private int connectToId;

    @JsonProperty("PATHWAY_TYPE_CONNECT")
    private PathwayType pathwayType;

    @JsonProperty("PATHWAYID")
    private String pathwayId;
    @JsonProperty("STAIR2ID")
    private int stair2Id;
    @JsonProperty("STAIR3ID")
    private int stair3Id;
    @JsonProperty("STAIR4ID")
    private int stair4Id;

    @JsonProperty("SIGN_POSTED_AS_FROM")
    private String signpostedAsFrom;
    @JsonProperty("SIGN_POSTED_AS_TO")
    private String signpostedAsTo;

    @JsonProperty("LINE_BULLETS_FROM")
    private String lineBulletsFrom;
    @JsonProperty("LINE_BULLETS_TO")
    private String lineBulletsTo;

    @JsonProperty("DIRECTIONAL_ARROW_FROM")
    private DirectionalArrow directionalArrowFrom;
    @JsonProperty("DIRECTIONAL_ARROW_TO")
    private DirectionalArrow directionalArrowTo;

    @JsonProperty("ADDITIONAL_INSTRUCTIONS_FROM")
    private String additionalInstructionsFrom;
    @JsonProperty("ADDITIONAL_INSTRUCTIONS_TO")
    private String additionalInstructionsTo;

    @JsonProperty("ELEVATOR_BUTTON_FROM")
    private String elevatorButtonFrom;
    @JsonProperty("ELEVATOR_BUTTON_TO")
    private String elevatorButtonTo;

}
