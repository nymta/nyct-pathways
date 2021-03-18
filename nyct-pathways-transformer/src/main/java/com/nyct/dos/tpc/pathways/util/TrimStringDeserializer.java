package com.nyct.dos.tpc.pathways.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

public class TrimStringDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(final JsonParser jsonParser, DeserializationContext ctx) throws IOException {
        return jsonParser.getValueAsString().trim();
    }
}
