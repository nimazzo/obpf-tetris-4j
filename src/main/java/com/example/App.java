package com.example;

import com.example.ui.Tetrion;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.util.stream.Stream;

public class App extends Application {
    public static final int NUM_PLAYERS = 2;
    private Simulator simulator;

    public static void main(String[] args) {
        launch(args);
    }

    private Scene createContent() {
        var tetrions = Stream.generate(Tetrion::new).limit(NUM_PLAYERS).toList();
        simulator = new Simulator(tetrions);

        var hbox = new HBox(10.0);
        hbox.getChildren().addAll(tetrions);
        var scene = new Scene(hbox);

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

        return scene;
    }

    @Override
    public void start(Stage stage) {
        stage.setScene(createContent());
        stage.setTitle("Obpf TetrisJ");

        stage.sizeToScene();
        stage.show();

        new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        simulator.tryConnect();
                        return null;
                    }
                };
            }
        }.start();

        stage.setOnCloseRequest(_ -> simulator.stopSimulating());
    }
}
