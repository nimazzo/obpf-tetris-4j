package com.example;

import com.example.network.GameServerConnection;
import com.example.network.NOOPGameServerConnection;
import com.example.simulation.Simulator;
import com.example.ui.Tetrion;
import com.example.worker.Worker;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.DataInputStream;
import java.net.Socket;
import java.util.stream.Stream;

public class App extends Application {
    public static final int NUM_PLAYERS = 1;
    private static final boolean SINGLE_PLAYER = NUM_PLAYERS == 1;
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

        Worker.execute(this::connectToLobbyServer);
        stage.setOnCloseRequest(_ -> simulator.stopSimulating());
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
        var tetrions = Stream.generate(Tetrion::new).limit(NUM_PLAYERS).toList();
        simulator = new Simulator(tetrions);

        var hbox = new HBox(10.0);
        hbox.getChildren().addAll(tetrions);
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
