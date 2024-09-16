package com.example.ui;

import com.example.state.AppState;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SceneManager {
    private static final Logger log = Logger.getLogger(SceneManager.class.getName());

    private final Map<AppState, AppScene> scenes = new HashMap<>();

    private AppState activeState = AppState.NONE;

    private final Stage stage;
    private final StackPane content = new StackPane();

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void switchAppState(AppState newState) {
        log.info("Switching from state " + activeState + " to " + newState);

        var previousScene = scenes.get(activeState);
        if (previousScene != null) {
            previousScene.onExit();
        }

        var newScene = scenes.get(newState);
        newScene.onEnter();

        content.getChildren().clear();
        content.getChildren().add(newScene.getNode());
        stage.sizeToScene();

        activeState = newState;
    }

    public void registerScene(AppScene scene) {
        scenes.put(scene.getState(), scene);
    }

    public StackPane getContent() {
        return content;
    }

    public void toggleGameMenu() {
        switchAppState(activeState == AppState.GAME_MENU ?
                AppState.GAME : AppState.GAME_MENU);
    }
}
