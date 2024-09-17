package com.example.state;

import com.example.daos.Lobby;
import com.example.network.ConnectionInfo;
import com.example.simulation.GameMode;
import com.example.simulation.Stats;

public enum GameState {
    INSTANCE;

    private volatile int numberOfPlayers;
    private volatile GameMode gameMode;
    private volatile Stats gameStats = new Stats(0, 0, 0, 0);
    private volatile Lobby lobby;
    private volatile ConnectionInfo connection;
    private volatile boolean isRunning;

    public void reset() {
        numberOfPlayers = 0;
        gameMode = null;
        gameStats = new Stats(0, 0, 0, 0);
        lobby = null;
        connection = null;
        isRunning = false;
    }

    public int getNumberOfPlayers() {
        return numberOfPlayers;
    }

    public void setNumberOfPlayers(int numberOfPlayers) {
        this.numberOfPlayers = numberOfPlayers;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public void setConnection(ConnectionInfo connection) {
        this.connection = connection;
    }

    public void setIsRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }

    public ConnectionInfo getConnection() {
        return connection;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public Lobby getLobby() {
        return lobby;
    }

    public void setLobby(Lobby lobby) {
        this.lobby = lobby;
    }

    public Stats getGameStats() {
        return gameStats;
    }

    public void setGameStats(Stats gameStats) {
        this.gameStats = gameStats;
    }
}
