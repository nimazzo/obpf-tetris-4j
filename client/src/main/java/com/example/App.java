package com.example;

import com.example.state.AppState;
import com.example.ui.SceneManager;
import com.example.ui.controllers.GameController;
import com.example.ui.controllers.LobbyController;
import com.example.ui.controllers.RootController;
import com.example.ui.views.game.GameScene;
import com.example.ui.views.menu.GameMenu;
import com.example.ui.views.menu.LobbyMenu;
import com.example.ui.views.menu.MainMenu;
import javafx.application.Application;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        var sceneManager = new SceneManager(stage);

        var scene = createScene(sceneManager.getContent());

        var mainMenu = new MainMenu();
        var gameScene = new GameScene();
        var lobbyMenu = new LobbyMenu();
        var gameMenu = new GameMenu();

        var gameController = new GameController(gameScene);
        var rootController = new RootController(gameController, sceneManager);
        var lobbyController = new LobbyController(lobbyMenu);

        sceneManager.registerScene(gameScene);
        sceneManager.registerScene(mainMenu);
        sceneManager.registerScene(lobbyMenu);
        sceneManager.registerScene(gameMenu);

        mainMenu.setOnSinglePlayerButtonClicked(() -> {
            rootController.startNewSinglePlayerGame();
            sceneManager.switchAppState(AppState.GAME);
        });

        mainMenu.setOnMultiPlayerButtonClicked(() -> {
            lobbyController.fetchLobbies();
            sceneManager.switchAppState(AppState.LOBBIES);
        });

        gameMenu.setOnReturnToGameButtonClicked(() -> {
            sceneManager.switchAppState(AppState.GAME);
            gameController.togglePause();
        });

        gameMenu.setOnLeaveGameButtonClicked(() -> {
            sceneManager.switchAppState(AppState.MAIN_MENU);
            gameController.stopSimulating();
        });

        lobbyMenu.setOnReturnToMainMenuButtonClicked(() -> sceneManager.switchAppState(AppState.MAIN_MENU));
        lobbyMenu.setOnUpdateButtonClicked(lobbyController::fetchLobbies);
        lobbyMenu.setOnCreateLobbyRequest(lobbyController::createNewLobby);

        sceneManager.switchAppState(AppState.MAIN_MENU);

        setupKeyboardInput(scene, rootController);

        stage.setOnCloseRequest(_ -> rootController.onApplicationClose());
        stage.setTitle("Obpf TetrisJ");
        stage.setScene(scene);
        stage.show();
    }

    private Scene createScene(StackPane content) {
        var root = new StackPane();
        var background = createBackground();
        root.getChildren().addAll(background, content);
        return new Scene(root);
    }

    private Node createBackground() {
        var background = new Pane();
        background.setEffect(new javafx.scene.effect.GaussianBlur(2));
        var img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("background.jpeg")));
        var backgroundImg = new BackgroundImage(img, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                new BackgroundSize(500, 500, false, false, false, false));
        background.setBackground(new Background(backgroundImg));
        return background;
    }

    private void setupKeyboardInput(Scene scene, RootController rootController) {
        scene.setOnKeyPressed(rootController::onKeyPressed);
        scene.setOnKeyReleased(rootController::onKeyReleased);
    }
}
