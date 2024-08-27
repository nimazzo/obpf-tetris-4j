package com.example.network;

import java.util.Optional;

public interface ServerConnection {
    void connect();

    void stop();

    void addHeartbeatMessage(ServerMessage.HeartbeatMessage message);

    ServerMessage waitForMessage();

    Optional<ServerMessage> pollMessage();
}
