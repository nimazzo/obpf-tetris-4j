package com.example.daos;

public record Lobby(
        Long id,
        String name,
        String owner,
        int numberOfPlayers,
        int maxPlayers,
        String gameServerHost,
        int gameServerPort
) {
}