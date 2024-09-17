package com.example.daos;

public record LobbyCreationRequest(
        String name,
        Integer maxPlayers
) {
}
