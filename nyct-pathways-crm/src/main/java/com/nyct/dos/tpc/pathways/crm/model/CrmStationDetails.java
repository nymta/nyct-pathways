package com.nyct.dos.tpc.pathways.crm.model;

import lombok.Value;

import java.util.List;

@Value
public class CrmStationDetails {
    List<CrmStationDetail> stations;
}
