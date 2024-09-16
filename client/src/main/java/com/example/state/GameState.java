package com.example.state;

import com.example.network.ConnectionInfo;
import com.example.simulation.GameMode;

public enum GameState {
    INSTANCE;

    private volatile int numberOfPlayers;
    private volatile GameMode gameMode;
    private volatile ConnectionInfo connection;
    private volatile boolean isRunning;

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
}
