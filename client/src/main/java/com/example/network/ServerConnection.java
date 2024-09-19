package com.example.network;

import com.example.simulation.Simulator;

import java.util.Optional;

public interface ServerConnection {
    void connect();

    void stop();

    void addHeartbeatMessage(ServerMessage.HeartbeatMessage message);

    ServerMessage waitForMessage();

    Optional<ServerMessage> pollMessage();

    void setSimulator(Simulator simulator);
}
