package com.example;

import com.example.network.FakeLobbyServer;
import com.example.ui.Tetrion;
import javafx.application.Application;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

public class App extends Application {
    private Tetrion tetrion;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        tetrion = new Tetrion();
        var hbox = new HBox(10.0);
        hbox.getChildren().addAll(tetrion, new Tetrion());
        Scene scene = new Scene(hbox);
        stage.setScene(scene);
        stage.setTitle("Obpf TetrisJ");

        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT -> tetrion.setKeyState(0, true);
                case RIGHT -> tetrion.setKeyState(1, true);
                case DOWN -> tetrion.setKeyState(2, true);
                case SPACE -> tetrion.setKeyState(3, true);
                case UP -> tetrion.setKeyState(4, true);
                case CONTROL -> tetrion.setKeyState(5, true);
                case ENTER -> tetrion.setKeyState(6, true);
            }
        });

        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT -> tetrion.setKeyState(0, false);
                case RIGHT -> tetrion.setKeyState(1, false);
                case DOWN -> tetrion.setKeyState(2, false);
                case SPACE -> tetrion.setKeyState(3, false);
                case UP -> tetrion.setKeyState(4, false);
                case CONTROL -> tetrion.setKeyState(5, false);
                case ENTER -> tetrion.setKeyState(6, false);
            }
        });

        stage.sizeToScene();
        stage.show();

        var service = new Service<Void>() {
            @Override
            protected Task<Void> createTask() {
                return new Task<>() {
                    @Override
                    protected Void call() {
                        var port = FakeLobbyServer.setupLobby(1);
                        tetrion.gameStarted(port);
                        return null;
                    }
                };
            }
        };
        service.start();
    }
}
