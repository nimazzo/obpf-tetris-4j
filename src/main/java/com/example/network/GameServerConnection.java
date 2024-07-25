package com.example.network;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class GameServerConnection extends Service<Void> {
    private Socket socket;
    private static final Object WRITE_LOCK = new Object();
    private final LinkedBlockingQueue<ServerMessage> messageQueue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<ServerMessage.HeartbeatMessage> heartbeatQueue = new LinkedBlockingQueue<>();
    private long playerID;

    private final AtomicBoolean running = new AtomicBoolean(true);

    private final int gamePort;

    public GameServerConnection(int gamePort) {
        this.gamePort = gamePort;
    }

    private void connect() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            socket = new Socket("localhost", gamePort);
            System.out.println("Connected to server");
            executor.execute(this::readServerMessages);
            executor.execute(this::sendHeartbeats);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void stop() {
        try {
            running.set(false);
            socket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void addHeartbeatMessage(ServerMessage.HeartbeatMessage message) {
        heartbeatQueue.add(message);
    }

    public ServerMessage pollMessage() {
        try {
            return messageQueue.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private void readServerMessages() {
        try {
            var in = socket.getInputStream();

            int messageType;
            while (running.get() && (messageType = in.read()) != -1) {
                switch (MessageType.fromValue(messageType)) {
                    case GameStart -> readGameStartMessage(in);
                    case StateBroadcast -> readStateBroadcast(in);
                    case Heartbeat, GridState ->
                            throw new RuntimeException("Unexpected MessageType with ID " + messageType);
                    case null -> throw new RuntimeException("Unexpected MessageType with ID " + messageType);
                }
            }
        } catch (IOException e) {
            if (running.get()) {
                throw new RuntimeException(e);
            }
        }
    }

    private void readGameStartMessage(InputStream in) {
        try {
            // payload size, can be ignored
            var _ = in.readNBytes(2);
            var clientId = in.read();
            playerID = clientId;
            var startFrame = new BigInteger(1, in.readNBytes(8)).longValue();
            var seed = new BigInteger(1, in.readNBytes(8));

            messageQueue.add(new ServerMessage.GameStart(clientId, startFrame, seed));

            synchronized (WRITE_LOCK) {
                System.out.println("------------- Client <" + clientId + "> Game Start Message -------------");
                System.out.println("clientId: " + clientId);
                System.out.println("startFrame: " + startFrame);
                System.out.println("seed: " + seed);
                System.out.println("------------- END -------------\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void readStateBroadcast(InputStream in) {
        try {
            // payload size, can be ignored
            var _ = in.readNBytes(2);
            var messageFrame = new BigInteger(1, in.readNBytes(8));
            var numClients = in.read();
            var clientStates = new LinkedHashMap<Integer, List<Integer>>(numClients);
            for (int i = 0; i < numClients; i++) {
                var clientId = in.read();
                var states = new ArrayList<Integer>(15);
                for (int j = 0; j < 15; j++) {
                    states.add(in.read());
                }
                clientStates.put(clientId, states);
            }

            messageQueue.add(new ServerMessage.StateBroadcast(messageFrame, numClients, clientStates));

            synchronized (WRITE_LOCK) {
                System.out.println("------------- Client <" + playerID + "> State Broadcast Message -------------");
                System.out.println("messageFrame: " + messageFrame);
                System.out.println("numClients: " + numClients);
                clientStates.forEach((clientId, keyStates) -> System.out.println("Client: " + clientId + " key states: " + keyStates));
                System.out.println("------------- END -------------\n");
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHeartbeats() {
        var messageBuffer = new ByteArrayOutputStream();
        var serializer = new TypeSerializer();

        while (running.get()) {
            try {
                var heartbeatMessage = heartbeatQueue.take();
                var keyStatesBuffer = heartbeatMessage.keyStatesBuffer();
                var frame = heartbeatMessage.frame();

                // message type
                messageBuffer.write(0);
                // payload size
                messageBuffer.writeBytes(serializer.serializeShort((short) (8 + keyStatesBuffer.size())));
                // frame
                messageBuffer.writeBytes(serializer.serializeLong(frame));
                // key states
                for (var keyStates : keyStatesBuffer) {
                    int keyState = 0;
                    for (byte i = 0; i < keyStates.length; i++) {
                        keyState |= (keyStates[i] << i);
                    }
                    messageBuffer.write(keyState);
                }

                // send heartbeat message
                System.out.println("Sending heatbeat message...");
                var out = socket.getOutputStream();
                out.write(messageBuffer.toByteArray());
            } catch (IOException e) {
                System.out.println("Server has closed the connection");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            messageBuffer.reset();
        }
    }

    @Override
    protected Task<Void> createTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                connect();
                return null;
            }
        };
    }
}
