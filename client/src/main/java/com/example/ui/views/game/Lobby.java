package com.example.ui.views.game;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Lobby(
        String name,
        String owner,
        int numberOfPlayers,
        int maxPlayers,
        String gameServerHost,
        int gameServerPort
) {
}
