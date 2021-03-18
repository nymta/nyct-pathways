package com.nyct.dos.tpc.pathways.crm.model;

import lombok.Value;

import java.util.List;

@Value
public class CrmStationDetail {
    int id;
    String name;
    String borough;
    double lat;
    double lng;
    //polygon?
    List<CrmPlatform> platforms;
    List<CrmEntrance> entrances;
}
