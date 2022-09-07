package com.macho.muscle.core.utils;

import com.google.gson.Gson;

public class JsonUtil {
    private static final Gson GSON = new Gson();

    public static <T> String toJsonString(T data) {
        return GSON.toJson(data);
    }

    public static <T> T fromJsonString(String json, Class<T> clazz) {
        return GSON.fromJson(json, clazz);
    }
}
