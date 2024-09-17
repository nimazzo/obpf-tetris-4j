package com.example;

import com.example.state.AppState;
import com.example.ui.SceneManager;
import com.example.ui.controllers.GameController;
import com.example.ui.controllers.LobbyController;
import com.example.ui.controllers.RootController;
import com.example.ui.views.game.Colors;
import com.example.ui.views.game.GameScene;
import com.example.ui.views.menu.GameMenu;
import com.example.ui.views.menu.LobbyMenu;
import com.example.ui.views.menu.LoginHeader;
import com.example.ui.views.menu.MainMenu;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Objects;

public class App extends Application {
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        var sceneManager = new SceneManager(stage);

        var mainMenu = new MainMenu();
        var gameScene = new GameScene();
        var lobbyMenu = new LobbyMenu();
        var gameMenu = new GameMenu();

        var gameController = new GameController(gameScene, sceneManager);
        var lobbyController = new LobbyController(lobbyMenu, sceneManager, gameController);
        var rootController = new RootController(gameController, lobbyController, sceneManager);

        var loginHeader = new LoginHeader(lobbyController.userInfoProperty());

        sceneManager.registerScene(gameScene);
        sceneManager.registerScene(mainMenu);
        sceneManager.registerScene(lobbyMenu);
        sceneManager.registerScene(gameMenu);

        mainMenu.setOnSinglePlayerButtonClicked(gameController::startNewSinglePlayerGame);
        mainMenu.setOnMultiPlayerButtonClicked(lobbyController::fetchLobbies);
        gameMenu.setOnReturnToGameButtonClicked(gameController::returnToGame);
        gameMenu.setOnLeaveGameButtonClicked(rootController::leaveGame);

        lobbyMenu.setOnReturnToMainMenuButtonClicked(lobbyController::returnToMainMenu);
        lobbyMenu.setOnUpdateButtonClicked(lobbyController::fetchLobbies);
        lobbyMenu.setOnCreateLobbyRequest(lobbyController::createNewLobby);
        lobbyMenu.setOnJoinLobbyButtonClicked(lobbyController::joinLobby);

        loginHeader.visibleProperty().bind(sceneManager.activeStateProperty().isNotEqualTo(AppState.GAME));
        loginHeader.setOnLogin(lobbyController::login);
        loginHeader.setOnLogout(lobbyController::logout);

        var scene = createScene(loginHeader, sceneManager.getContent());

        setupKeyboardInput(scene, rootController);

        stage.setOnCloseRequest(_ -> rootController.onApplicationClose());
        stage.setTitle("Obpf TetrisJ");
        stage.setScene(scene);

        sceneManager.switchAppState(AppState.MAIN_MENU);

        stage.show();
    }

    private Scene createScene(LoginHeader header, StackPane content) {
        var root = new StackPane();
        var background = createBackground();

        var headerBox = new VBox(header);
        headerBox.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 0, 4, 0))));
        headerBox.setAlignment(Pos.TOP_RIGHT);
        headerBox.setBackground((Background.fill(Colors.CLEAR_COLOR.deriveColor(0, 1, 1, 0.6))));
        headerBox.visibleProperty().bind(header.visibleProperty());
        headerBox.managedProperty().bind(header.visibleProperty());

        var center = new VBox(headerBox, content);

        root.getChildren().addAll(background, center);
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
