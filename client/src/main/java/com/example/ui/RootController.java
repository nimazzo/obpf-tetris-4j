package com.example.ui;

import com.example.state.GameState;
import com.example.ui.game.GameController;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class RootController {

    private final GameController gameController;
    private final SceneManager sceneManager;

    public RootController(GameController gameController, SceneManager sceneManager) {
        this.gameController = gameController;
        this.sceneManager = sceneManager;
    }

    public void startNewSinglePlayerGame() {
        gameController.startNewSinglePlayerGame();
    }

    public void onApplicationClose() {
        gameController.stopSimulating();
    }

    public void onKeyPressed(KeyEvent e) {
        if (e.getCode() == KeyCode.ESCAPE) {
            handleEscapePressed();
        } else {
            gameController.onKeyPressed(e);
        }
    }

    public void onKeyReleased(KeyEvent e) {
        gameController.onKeyReleased(e);
    }

    private void handleEscapePressed() {
        if (GameState.INSTANCE.isRunning()) {
            gameController.togglePause();
            sceneManager.toggleGameMenu();
        }
    }
}
