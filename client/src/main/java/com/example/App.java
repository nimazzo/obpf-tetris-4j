package com.example;

import com.example.network.GameServerConnection;
import com.example.network.NOOPGameServerConnection;
import com.example.simulation.Simulator;
import com.example.ui.Tetrion;
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
    public static final int NUM_PLAYERS = 1;
    private static final boolean SINGLE_PLAYER = NUM_PLAYERS == 1;
    private Simulator simulator;

    // UI elements
    private final List<Tetrion> tetrions = new ArrayList<>(NUM_PLAYERS);
    private final IntegerProperty fpsProperty = new SimpleIntegerProperty(0);

    // fps calculation
    private long totalFrameTime = 0;
    private int frameTimeIndex = 0;
    private long last = System.nanoTime();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(createContent());
        stage.setTitle("Obpf TetrisJ");

        stage.sizeToScene();
        stage.show();

        Worker.execute(this::connectToLobbyServer);
        stage.setOnCloseRequest(_ -> simulator.stopSimulating());

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                redraw(now);
            }
        }.start();
    }

    private void connectToLobbyServer() {
        if (SINGLE_PLAYER) {
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
        Stream.generate(Tetrion::new).limit(NUM_PLAYERS).forEach(tetrions::add);
        simulator = new Simulator(tetrions);

        var tetrionsBox = new HBox(10.0);
        tetrionsBox.getChildren().addAll(tetrions);

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

        var content = new VBox(10.0, tetrionsBox, fpsBox);
        root.getChildren().addAll(background, content);
        var scene = new Scene(root);

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
}
