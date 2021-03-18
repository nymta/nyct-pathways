package com.nyct.dos.tpc.pathways;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.nyct.dos.tpc.pathways.model.eerms.Equipment;
import com.nyct.dos.tpc.pathways.model.eerms.EquipmentList;
import com.nyct.dos.tpc.pathways.util.TrimStringDeserializer;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;

import static com.google.common.collect.ImmutableMap.toImmutableMap;

@UtilityClass
public class EquipmentLoader {
    static Map<String, Equipment> loadEquipment(File equipmentFile) throws IOException {
        ObjectMapper xmlMapper = new XmlMapper();
        xmlMapper.registerModule(
                new SimpleModule("TrimStringModule")
                        .addDeserializer(String.class, new TrimStringDeserializer())
        );
        EquipmentList equipmentList = xmlMapper.readValue(equipmentFile, EquipmentList.class);

        Map<String, Equipment> equipmentMap = equipmentList.getEquipmentList().stream()
                .collect(toImmutableMap(Equipment::getEquipmentNumber, Function.identity()));

        return equipmentMap;
    }

}
