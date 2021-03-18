package com.nyct.dos.tpc.pathways.crm.model;

import lombok.Value;

import java.util.List;

@Value
public class CrmBorough {
    String name;
    List<CrmStation> stations;

}
