package com.example.network;

import java.util.LinkedHashMap;
import java.util.List;

public sealed interface ServerMessage {
    record GameStartMessage(int clientId, long startFrame, long seed) implements ServerMessage {
    }

    record StateBroadcastMessage(long messageFrame, int numClients,
                                 LinkedHashMap<Integer, List<Integer>> clientStates) implements ServerMessage {
    }

    record HeartbeatMessage(long frame, List<int[]> keyStatesBuffer) implements ServerMessage {
    }
}

