package com.mar.utils;

import lombok.experimental.UtilityClass;
import tools.jackson.databind.ObjectMapper;

import java.io.Serializable;

@UtilityClass
public class JsonUtility {

    private final ObjectMapper mapper;

    static {
        mapper = new ObjectMapper();
    }

    public static <T extends Serializable> T get(String json, Class<T> clazz) {
        return mapper.readValue(json, clazz);
    }

    public static String getJSON(Object obj) {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
    }

}
