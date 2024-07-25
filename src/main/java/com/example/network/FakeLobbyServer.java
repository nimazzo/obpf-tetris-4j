package com.example.network;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;

public class FakeLobbyServer {
    public static int setupLobby(int numPlayers) {
        System.out.println("Listening on 8080");
        try (var server = new ServerSocket(8080)) {
            var socket = server.accept();
            System.out.println("GameServer connected");

            var out = new DataOutputStream(socket.getOutputStream());
            var in = new DataInputStream(socket.getInputStream());
            out.writeShort(numPlayers);
            return in.readShort() & 0xFFFF;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
