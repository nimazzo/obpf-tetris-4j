package com.example.ui;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;

public class ErrorMessages {

    public static void showErrorMessage(String summary, String message) {
        var threadName = Thread.currentThread().getName();

        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("An Error Occurred on thread: " + threadName);
            alert.setResizable(true);
            alert.setHeaderText(summary.substring(0, Math.min(summary.length(), 120))
                                + (summary.length() > 120 ? "..." : ""));

            var errorMessage = new TextArea(message);
            var scrollPane = new ScrollPane(errorMessage);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            alert.getDialogPane().setContent(scrollPane);

            alert.showAndWait();
        });
    }
}
