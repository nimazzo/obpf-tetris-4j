package com.example.ui.views.menu;

import com.example.daos.UserInfo;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import static com.example.ui.TextFactory.createText;

public class LoginHeader extends HBox {

    private Runnable loginClb = () -> {
        throw new IllegalStateException("Login callback not set");
    };
    private Runnable logoutClb = () -> {
        throw new IllegalStateException("Logout callback not set");
    };

    public LoginHeader(SimpleObjectProperty<UserInfo> userInfoProperty) {
        setPadding(new Insets(10));
        setSpacing(10);
        setAlignment(Pos.CENTER_RIGHT);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);

        var usernameText = createText("", FontWeight.NORMAL, 24, Color.WHITE, 1.0, Color.BLACK);
        var button = new Button();
        button.setFont(Font.font(15));

        usernameText.textProperty().bind(userInfoProperty.map(userInfo -> "Logged in as: " + userInfo.username()).orElse("Currently not logged in"));
        button.textProperty().bind(userInfoProperty.map(_ -> "Logout").orElse("Login"));

        button.setOnAction(_ -> {
            if (userInfoProperty.get() == null) {
                loginClb.run();
            } else {
                logoutClb.run();
            }
        });

        getChildren().addAll(usernameText, button);
    }

    public void setOnLogin(Runnable clb) {
        loginClb = clb;
    }

    public void setOnLogout(Runnable clb) {
        logoutClb = clb;
    }
}

