package com.macho.muscle.core.actor;

import com.google.common.collect.Maps;

import java.util.Map;

public enum ActorMessageType {
    SYSTEM_MESSAGE(1),
    USER_MESSAGE(2),

    ;

    private int code;

    ActorMessageType(int _code) {
        this.code = _code;
    }

    public int getCode() {
        return this.code;
    }

    private static final Map<Integer, ActorMessageType> codeToTypeMap;

    static {
        codeToTypeMap = Maps.newHashMap();

        for (ActorMessageType type : ActorMessageType.values()) {
            codeToTypeMap.put(type.code, type);
        }
    }

    public static final ActorMessageType valueOf(int code) {
        ActorMessageType type = codeToTypeMap.get(code);

        if (type == null) {
            throw new IllegalArgumentException("Invalid ActorMessageType code: " + code);
        }

        return type;
    }
}