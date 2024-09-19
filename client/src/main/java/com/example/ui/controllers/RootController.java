package com.example.ui.controllers;

import com.example.concurrent.Task;
import com.example.simulation.GameMode;
import com.example.state.AppState;
import com.example.state.GameState;
import com.example.ui.SceneManager;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class RootController {

    private final GameController gameController;
    private final LobbyController lobbyController;
    private final SceneManager sceneManager;

    public RootController(GameController gameController, LobbyController lobbyController, SceneManager sceneManager) {
        this.gameController = gameController;
        this.lobbyController = lobbyController;
        this.sceneManager = sceneManager;
    }

    public void onApplicationClose() {
        leaveLobbyAndStopSimulating().await();
    }

    public void leaveGame() {
        leaveLobbyAndStopSimulating();
        sceneManager.switchAppState(AppState.MAIN_MENU);
    }

    public void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            handleEscapePressed();
        } else if (GameState.INSTANCE.isRunning()) {
            gameController.onKeyPressed(e);
        }
    }

    public void onKeyReleased(KeyEvent e) {
        if (GameState.INSTANCE.isRunning()) {
            gameController.onKeyReleased(e);
        }
    }

    private void handleEscapePressed() {
        if (GameState.INSTANCE.getGameMode() != null) {
            gameController.togglePause();
            sceneManager.toggleGameMenu();
        } else {
            sceneManager.switchAppState(AppState.MAIN_MENU);
        }
    }

    private Task<Void> leaveLobbyAndStopSimulating() {
        return Task.runOnWorkerThread(() -> {
            gameController.leaveGame();
            if (GameState.INSTANCE.getGameMode() == GameMode.MULTIPLAYER) {
                lobbyController.leaveLobby().await();
            }
            GameState.INSTANCE.reset();
        });
    }
}
