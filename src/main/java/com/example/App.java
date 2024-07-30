package com.example;

import com.example.network.GameServerConnection;
import com.example.ui.Tetrion;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class App extends Application {
    public static final int NUM_PLAYERS = 2;
    private Simulator simulator;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(createContent());
        stage.setTitle("Obpf TetrisJ");

        stage.sizeToScene();
        stage.show();

        CompletableFuture.runAsync(this::connectToLobbyServer, Executors.newVirtualThreadPerTaskExecutor());

        stage.setOnCloseRequest(_ -> simulator.stopSimulating());
    }

    private void connectToLobbyServer() {
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
        var tetrions = new ArrayList<>(List.of(new Tetrion()));
        Stream.generate(Tetrion::new).limit(NUM_PLAYERS - 1).forEach(tetrions::add);
        simulator = new Simulator(tetrions);

        var hbox = new HBox(10.0, tetrions.getFirst());
        hbox.getChildren().addAll(tetrions.subList(1, tetrions.size()));
        var scene = new Scene(hbox);
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
}
