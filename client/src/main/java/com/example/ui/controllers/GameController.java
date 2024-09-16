package com.example.ui.controllers;

import com.example.concurrent.Worker;
import com.example.network.GameServerConnection;
import com.example.network.NOOPGameServerConnection;
import com.example.network.ServerConnection;
import com.example.simulation.GameMode;
import com.example.simulation.Simulator;
import com.example.state.GameState;
import com.example.ui.views.game.GameScene;
import javafx.scene.input.KeyEvent;

public class GameController {

    private Simulator simulator;
    private final GameScene gameScene;

    public GameController(GameScene gameScene) {
        this.gameScene = gameScene;
    }

    public void startNewSinglePlayerGame() {
        GameState.INSTANCE.setIsRunning(true);
        gameScene.init();
        simulator = new Simulator(gameScene.getTetrions());
        Worker.execute(() -> simulator.startSimulating(getConnection()));
    }

    public void stopSimulating() {
        GameState.INSTANCE.setIsRunning(false);
        if (simulator != null) {
            simulator.stopSimulating();
        }
    }

    private ServerConnection getConnection() {
        var mode = GameState.INSTANCE.getGameMode();

        return switch (mode) {
            case SINGLEPLAYER -> new NOOPGameServerConnection();
            case MULTIPLAYER -> {
                var connection = GameState.INSTANCE.getConnection();
                yield new GameServerConnection(connection.host(), connection.port());
            }
        };
    }

    public void onKeyPressed(KeyEvent e) {
        switch (e.getCode()) {
            case LEFT -> simulator.setKeyState(0, true);
            case RIGHT -> simulator.setKeyState(1, true);
            case DOWN -> simulator.setKeyState(2, true);
            case SPACE -> simulator.setKeyState(3, true);
            case UP -> simulator.setKeyState(4, true);
            case CONTROL -> simulator.setKeyState(5, true);
            case ENTER -> simulator.setKeyState(6, true);
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
}
