package com.example.network;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {
    Heartbeat(0),
    GridState(1),
    GameStart(2),
    StateBroadcast(3);

    private final int value;

    private static final Map<Integer, MessageType> map = new HashMap<>();

    static {
        for (var type : MessageType.values()) {
            map.put(type.value, type);
        }
    }

    MessageType(int value) {
        this.value = value;
    }

    public static MessageType fromValue(int value) {
        return map.get(value);
    }
}