package com.nyct.dos.tpc.pathways.crm.model;


import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;

import java.util.List;

@Value
public class CrmLine {
    String line;
    @JsonProperty("boro")
    List<CrmBorough> boroughs;
}
