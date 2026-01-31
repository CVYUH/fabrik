package com.cvyuh.utils.misc;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public final class Json {

    public static final ObjectMapper MAPPER = new ObjectMapper();

    private Json() {}

    public static ObjectNode parseObject(String json) {
        try {
            JsonNode node = MAPPER.readTree(json);
            return node instanceof ObjectNode ? (ObjectNode) node : null;
        } catch (Exception e) {
            return null;
        }
    }

    public static String stringify(JsonNode node) {
        try {
            return MAPPER.writeValueAsString(node);
        } catch (Exception e) {
            return null;
        }
    }
}