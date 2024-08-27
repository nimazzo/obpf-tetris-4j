package com.example.network;

import java.util.Optional;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class NOOPGameServerConnection implements ServerConnection {

    BlockingQueue<ServerMessage> messageQueue = new LinkedBlockingQueue<>();

    @Override
    public void connect() {
        var random = new Random();
        messageQueue.add(new ServerMessage.GameStartMessage(0, 180, random.nextLong()));
    }

    @Override
    public void stop() {
    }

    @Override
    public void addHeartbeatMessage(ServerMessage.HeartbeatMessage message) {
    }

    @Override
    public ServerMessage waitForMessage() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<ServerMessage> pollMessage() {
        return Optional.empty();
    }
}
