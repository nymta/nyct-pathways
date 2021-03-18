package com.nyct.dos.tpc.pathways.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.ContextualDeserializer;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
public class StringBooleanDeserializer extends JsonDeserializer<Boolean> implements ContextualDeserializer {
    private final List<String> trueValues;
    private final List<String> falseValues;

    public StringBooleanDeserializer() {
        trueValues = null;
        falseValues = null;
    }

    @Override
    public Boolean deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        if (trueValues.contains(parser.getText())) {
            return true;
        } else if (falseValues.contains(parser.getText())) {
            return false;
        } else {
            throw context.weirdStringException(parser.getText(), Boolean.class,
                    String.format("String was not one of true values: %s or false values: %s", String.join(",", trueValues), String.join(",", falseValues)));
        }
    }

    @Override
    public JsonDeserializer<?> createContextual(DeserializationContext ctxt, BeanProperty property) {
        BooleanValues annotation = property.getAnnotation(BooleanValues.class);

        if (annotation != null) {
            List<String> trueValues = ImmutableList.copyOf(annotation.trueValues());
            List<String> falseValues = ImmutableList.copyOf(annotation.falseValues());
            return new StringBooleanDeserializer(trueValues, falseValues);
        } else {
            return this;
        }
    }
}
