package com.nyct.dos.tpc.pathways.model.eerms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.nyct.dos.tpc.pathways.types.EquipmentType;
import com.nyct.dos.tpc.pathways.util.BooleanValues;
import com.nyct.dos.tpc.pathways.util.StringBooleanDeserializer;
import lombok.Data;

@Data
@JacksonXmlRootElement(localName = "equipment")
@JsonNaming(LowerCaseStrategy.class)
public class Equipment {
    private String station;
    private String borough;

    private EquipmentType equipmentType;
    @JsonProperty("equipmentno")
    private String equipmentNumber;

    @JsonProperty("ADA")
    @JsonDeserialize(using = StringBooleanDeserializer.class)
    @BooleanValues(trueValues = {"Y"}, falseValues = {"N"})
    private boolean isADA;
    @JsonDeserialize(using = StringBooleanDeserializer.class)
    @BooleanValues(trueValues = {"Y"}, falseValues = {"N"})
    private boolean isActive;
    @JsonProperty("nonNYCT")
    @JsonDeserialize(using = StringBooleanDeserializer.class)
    @BooleanValues(trueValues = {"Y"}, falseValues = {"N"})
    private boolean isNonNyct;

    private String serving;
    private String shortDescription;
    @JsonProperty("trainno")
    private String linesServedByStation;
    private String linesServedByElevator;
    @JsonProperty("elevatorsgtfsstopid")
    private String elevatorGtfsStopId;
    private String elevatorMrn;
    @JsonProperty("stationcomplexid")
    private int stationComplexMrn;

    private String nextAdaNorth;
    private String nextAdaSouth;

    @JsonDeserialize(using = StringBooleanDeserializer.class)
    @BooleanValues(trueValues = {"1"}, falseValues = {"0"})
    private boolean redundant;

    private String busConnections;
    private String alternativeRoute;
}
