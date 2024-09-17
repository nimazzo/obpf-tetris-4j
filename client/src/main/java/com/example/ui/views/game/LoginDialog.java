package com.example.ui.views.game;

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import org.springframework.http.HttpHeaders;

import java.nio.charset.Charset;

public class LoginDialog extends Dialog<String> {

    private final TextField usernameInput = new TextField();
    private final TextField passwordInput = new PasswordField();

    public LoginDialog() {
        setTitle("Login");
        setHeaderText("Please enter your credentials");

        var loginButtonType = new ButtonType("Login", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(loginButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Username:"), 0, 0);
        grid.add(usernameInput, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordInput, 1, 1);

        var loginButton = getDialogPane().lookupButton(loginButtonType);
        loginButton.setDisable(true);

        usernameInput.textProperty().addListener((_, _, _) ->
                loginButton.setDisable(invalidInputs()));

        passwordInput.textProperty().addListener((_, _, _) ->
                loginButton.setDisable(invalidInputs()));

        getDialogPane().setContent(grid);

        usernameInput.requestFocus();

        setResultConverter(dialogButton -> {
            if (dialogButton == loginButtonType) {
                return HttpHeaders.encodeBasicAuth(usernameInput.getText(), passwordInput.getText(), Charset.defaultCharset());
            }
            return null;
        });
    }

    private boolean invalidInputs() {
        var username = usernameInput.getText();
        var password = passwordInput.getText();

        return username.isBlank() || password.isBlank();
    }
}
