package com.example.ui.controllers;

import com.example.concurrent.Task;
import com.example.daos.Lobby;
import com.example.network.ConnectionInfo;
import com.example.network.GameServerConnection;
import com.example.network.NOOPGameServerConnection;
import com.example.network.ServerConnection;
import com.example.simulation.GameMode;
import com.example.simulation.Simulator;
import com.example.state.AppState;
import com.example.state.GameState;
import com.example.ui.SceneManager;
import com.example.ui.views.game.GameScene;
import javafx.scene.input.KeyEvent;

public class GameController {

    private Simulator simulator;
    private final GameScene gameScene;
    private final SceneManager sceneManager;

    public GameController(GameScene gameScene, SceneManager sceneManager) {
        this.gameScene = gameScene;
        this.sceneManager = sceneManager;
    }

    public void startNewSinglePlayerGame() {
        GameState.INSTANCE.setGameMode(GameMode.SINGLEPLAYER);
        GameState.INSTANCE.setNumberOfPlayers(1);
        GameState.INSTANCE.setIsRunning(true);

        startGame();
    }

    public void joinMultiplayerGame(Lobby lobby, ConnectionInfo connectionInfo) {
        GameState.INSTANCE.setGameMode(GameMode.MULTIPLAYER);
        GameState.INSTANCE.setConnection(connectionInfo);
        GameState.INSTANCE.setLobby(lobby);
        GameState.INSTANCE.setNumberOfPlayers(lobby.maxPlayers());
        GameState.INSTANCE.setIsRunning(true);

        startGame();
    }

    public void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT -> simulator.setKeyState(0, true);
            case RIGHT -> simulator.setKeyState(1, true);
            case DOWN -> simulator.setKeyState(2, true);
            case SPACE -> simulator.setKeyState(3, true);
            case UP -> simulator.setKeyState(4, true);
            case CONTROL -> simulator.setKeyState(5, true);
            case ENTER, SHIFT -> simulator.setKeyState(6, true);
        }
    }

    public void onKeyReleased(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT -> simulator.setKeyState(0, false);
            case RIGHT -> simulator.setKeyState(1, false);
            case DOWN -> simulator.setKeyState(2, false);
            case SPACE -> simulator.setKeyState(3, false);
            case UP -> simulator.setKeyState(4, false);
            case CONTROL -> simulator.setKeyState(5, false);
            case ENTER -> simulator.setKeyState(6, false);
        }
    }

    public void togglePause() {
        var isSinglePlayer = GameState.INSTANCE.getGameMode() == GameMode.SINGLEPLAYER;
        if (isSinglePlayer) {
            simulator.togglePause();
        }
    }

    public void returnToGame() {
        sceneManager.switchAppState(AppState.GAME);
        togglePause();
    }

    public void leaveGame() {
        stopSimulating();
    }

    private void stopSimulating() {
        if (simulator != null) {
            simulator.stopSimulating();
            simulator = null;
        }
    }

    private void startGame() {
        gameScene.init();
        simulator = new Simulator(gameScene.getTetrions());
        Task.runOnWorkerThread(() -> simulator.startSimulating(getConnection()));

        sceneManager.switchAppState(AppState.GAME);
    }

    private ServerConnection getConnection() {
        var mode = GameState.INSTANCE.getGameMode();

        return switch (mode) {
            case SINGLEPLAYER -> new NOOPGameServerConnection();
            case MULTIPLAYER -> {
                var connection = GameState.INSTANCE.getConnection();
                yield new GameServerConnection(connection.hostname(), connection.port());
            }
        };
    }
}
