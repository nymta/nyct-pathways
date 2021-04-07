package com.nyct.dos.tpc.pathways.crm.model;

import lombok.Value;

import java.util.List;

@Value
public class CrmLine {
    String line;
    List<CrmBorough> boroughs;
}
