package com.example.ui;

import com.example.state.AppState;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class SceneManager {
    private static final Logger log = Logger.getLogger(SceneManager.class.getName());

    private final Map<AppState, AppScene> scenes = new HashMap<>();

    private final SimpleObjectProperty<AppState> activeState = new SimpleObjectProperty<>(AppState.NONE);

    private final Stage stage;
    private final StackPane content = new StackPane();

    public SceneManager(Stage stage) {
        this.stage = stage;
    }

    public void switchAppState(AppState newState) {
        if (newState == activeState.get()) {
            return;
        }

        log.info("Switching from state " + activeState + " to " + newState);

        var previousScene = scenes.get(activeState.get());
        if (previousScene != null) {
            previousScene.onExit();
        }

        var newScene = scenes.get(newState);
        if (!newScene.canEnter()) {
            return;
        }

        activeState.set(newState);
        content.getChildren().clear();
        content.getChildren().add(newScene.getNode());
        stage.sizeToScene();

        newScene.onEnter();
    }

    public void registerScene(AppScene scene) {
        scenes.put(scene.getState(), scene);
    }

    public StackPane getContent() {
        return content;
    }

    public SimpleObjectProperty<AppState> activeStateProperty() {
        return activeState;
    }

    public void toggleGameMenu() {
        switchAppState(activeState.get() == AppState.GAME_MENU ?
                AppState.GAME : AppState.GAME_MENU);
    }
}
