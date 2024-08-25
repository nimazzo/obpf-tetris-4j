package com.example.network;

import com.example.App;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class FakeLobbyServer {
    private volatile int port;
    private final CountDownLatch serverOnline = new CountDownLatch(1);
    private final AtomicInteger numClients = new AtomicInteger(0);
    private ProcessHandle handle;

    public FakeLobbyServer() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (handle != null) {
                System.out.println("Destroying GameServer");
                handle.destroy();
            }
        }));
    }

    public static void main(String[] args) {
        new FakeLobbyServer().setupLobby();
    }

    public void setupLobby() {
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            executor.execute(this::listenForGameServer);
            executor.execute(this::listenForClients);
            executor.execute(this::launchGameServer);
        }
    }

    private void launchGameServer() {
        try {
            var serverPath = Objects.requireNonNull(getClass().getResource("server.exe")).getPath();
            var process = new ProcessBuilder(serverPath, "8080")
                    .inheritIO()
                    .start();
            handle = process.toHandle();
            var exit = process.waitFor();
            System.out.println("GameServer exited with code " + exit);
        } catch (InterruptedException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void listenForGameServer() {
        System.out.println("Listening on 8080 for GameServer");
        try (var server = new ServerSocket(8080)) {
            var socket = server.accept();
            System.out.println("GameServer connected");

            var out = new DataOutputStream(socket.getOutputStream());
            var in = new DataInputStream(socket.getInputStream());
            out.writeShort(App.NUM_PLAYERS);
            port = in.readShort() & 0xFFFF;
            serverOnline.countDown();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Stopped listening for GameServer");
    }

    private void listenForClients() {
        System.out.println("Listening on 8081 for Clients");
        try (
                var executor = Executors.newVirtualThreadPerTaskExecutor();
                var server = new ServerSocket(8081)
        ) {
            while (numClients.get() < App.NUM_PLAYERS) {
                var socket = server.accept();
                numClients.incrementAndGet();
                executor.execute(() -> handleClient(socket));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        System.out.println("Stopped listening for Clients");
    }

    private void handleClient(Socket socket) {
        try {
            System.out.println("Client connected");
            var out = new DataOutputStream(socket.getOutputStream());
            serverOnline.await();
            out.writeInt(port);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
