package com.example;

import com.example.network.GameServerConnection;
import com.example.network.NOOPGameServerConnection;
import com.example.simulation.GameMode;
import com.example.simulation.Simulator;
import com.example.ui.Tetrion;
import com.example.ui.menu.MainMenu;
import com.example.worker.Worker;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static com.example.ui.TextFactory.createText;

public class App extends Application {
    public static final int NUM_PLAYERS = 2;
    private GameMode gameMode = GameMode.SINGLE_PLAYER;

    private Simulator simulator;

    // UI elements
    private Stage stage;
    private final List<Tetrion> tetrions = new ArrayList<>();
    private final IntegerProperty fpsProperty = new SimpleIntegerProperty(0);
    private final MainMenu mainMenu = new MainMenu();
    private final VBox gameContent = new VBox(10.0);
    private final HBox tetrionsBox = new HBox(10.0);

    // fps calculation
    private long totalFrameTime = 0;
    private int frameTimeIndex = 0;
    private long last = System.nanoTime();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.stage = stage;
        stage.setScene(createContent());
        stage.setTitle("Obpf TetrisJ");

        stage.sizeToScene();
        stage.show();

        simulator = new Simulator(tetrions);
        stage.setOnCloseRequest(_ -> simulator.stopSimulating());

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                redraw(now);
            }
        }.start();
    }

    private void connectToLobbyServer() {
        if (gameMode == GameMode.SINGLE_PLAYER) {
            System.out.println("Starting Single Player Mode...");
            simulator.startSimulating(new NOOPGameServerConnection());
            return;
        }

        System.out.println("Connecting to Server...");
        try (var socket = new Socket("localhost", 8081)) {
            var in = new DataInputStream(socket.getInputStream());
            var port = in.readInt();
            var conn = new GameServerConnection(port);
            simulator.startSimulating(conn);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Scene createContent() {
        var fpsText = createText("FPS:", FontWeight.EXTRA_BOLD, 30, Color.WHITE, 2.0, Color.BLACK);
        var fpsCounter = createText("0", FontWeight.EXTRA_BOLD, 30, Color.WHITE, 2.0, Color.BLACK);
        var fpsBox = new HBox(10.0, fpsText, fpsCounter);

        fpsCounter.textProperty().bind(Bindings.convert(fpsProperty));
        fpsBox.setPadding(new Insets(10.0));

        var root = new StackPane();

        var background = new Pane();
        background.setEffect(new javafx.scene.effect.GaussianBlur(2));
        var img = new Image(Objects.requireNonNull(getClass().getResourceAsStream("background.jpeg")));
        var backgroundImg = new BackgroundImage(img, BackgroundRepeat.REPEAT, BackgroundRepeat.REPEAT, BackgroundPosition.DEFAULT,
                new BackgroundSize(500, 500, false, false, false, false));
        background.setBackground(new Background(backgroundImg));

        gameContent.getChildren().addAll(tetrionsBox, fpsBox);
        gameContent.setVisible(false);
        root.getChildren().addAll(background, gameContent, mainMenu);

        var scene = new Scene(root);

        mainMenu.setOnSinglePlayerButtonClicked(this::startSinglePlayerGame);
        mainMenu.setOnMultiPlayerButtonClicked(this::startMultiPlayerGame);

        setupKeyboardInput(scene);
        return scene;
    }

    private void setupKeyboardInput(Scene scene) {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT -> simulator.setKeyState(0, true);
                case RIGHT -> simulator.setKeyState(1, true);
                case DOWN -> simulator.setKeyState(2, true);
                case SPACE -> simulator.setKeyState(3, true);
                case UP -> simulator.setKeyState(4, true);
                case CONTROL -> simulator.setKeyState(5, true);
                case ENTER -> simulator.setKeyState(6, true);
                case ESCAPE -> toggleMenu();
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT -> simulator.setKeyState(0, false);
                case RIGHT -> simulator.setKeyState(1, false);
                case DOWN -> simulator.setKeyState(2, false);
                case SPACE -> simulator.setKeyState(3, false);
                case UP -> simulator.setKeyState(4, false);
                case CONTROL -> simulator.setKeyState(5, false);
                case ENTER -> simulator.setKeyState(6, false);
            }
        });
    }

    private void toggleMenu() {
        if (gameContent.isVisible()) {
            gameContent.setVisible(false);
            mainMenu.setVisible(true);
        } else {
            gameContent.setVisible(true);
            mainMenu.setVisible(false);
        }
    }

    private void redraw(long now) {
        tetrions.forEach(Tetrion::redraw);

        totalFrameTime += now - last;
        frameTimeIndex++;
        last = now;

        if (totalFrameTime >= 1_000_000_000) {
            long averageFrameTime = totalFrameTime / frameTimeIndex;
            long fps = 1_000_000_000 / averageFrameTime;
            System.out.println("Num frames: " + frameTimeIndex + " total frame time: " + totalFrameTime + " average frame time: " + averageFrameTime);
            fpsProperty.set((int) fps);

            totalFrameTime = 0;
            frameTimeIndex = 0;
        }
    }

    private void prepareGame() {
        simulator.stopSimulating();

        tetrions.clear();
        tetrionsBox.getChildren().clear();
        var num = switch (gameMode) {
            case SINGLE_PLAYER -> 1;
            case MULTI_PLAYER -> NUM_PLAYERS;
        };
        Stream.generate(Tetrion::new).limit(num).forEach(tetrions::add);
        tetrionsBox.getChildren().addAll(tetrions);
        mainMenu.setVisible(false);
        gameContent.setVisible(true);
        stage.sizeToScene();
    }

    private void startSinglePlayerGame() {
        gameMode = GameMode.SINGLE_PLAYER;
        prepareGame();
        Worker.execute(this::connectToLobbyServer);
    }

    private void startMultiPlayerGame() {
        gameMode = GameMode.MULTI_PLAYER;
        prepareGame();
        Worker.execute(this::connectToLobbyServer);
    }
}
