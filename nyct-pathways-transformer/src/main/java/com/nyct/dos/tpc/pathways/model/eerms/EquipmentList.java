package com.nyct.dos.tpc.pathways.model.eerms;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies.LowerCaseStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import lombok.Data;

import java.util.List;

@Data
@JacksonXmlRootElement(localName = "NYCEquipments")
@JsonNaming(LowerCaseStrategy.class)
public class EquipmentList {
    private int responseCode;
    private String message;

    @JacksonXmlElementWrapper(useWrapping = false)
    @JsonProperty("equipment")
    private List<Equipment> equipmentList;
}
