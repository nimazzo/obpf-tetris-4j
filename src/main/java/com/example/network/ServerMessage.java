package com.example.network;

import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;

public sealed interface ServerMessage {
    record GameStart(int clientId, long startFrame, BigInteger seed) implements ServerMessage {
    }

    record StateBroadcast(BigInteger messageFrame, int numClients,
                          LinkedHashMap<Integer, List<Integer>> clientStates) implements ServerMessage {
    }

    record HeartbeatMessage(long frame, List<int[]> keyStatesBuffer) implements ServerMessage {
    }
}
