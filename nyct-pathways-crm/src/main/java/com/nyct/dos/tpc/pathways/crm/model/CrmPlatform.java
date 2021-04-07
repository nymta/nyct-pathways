package com.nyct.dos.tpc.pathways.crm.model;

import lombok.Value;

@Value
public class CrmPlatform {
    String id;
    String line;
    String bound;
    String service;
    String label;
    String name;
}
