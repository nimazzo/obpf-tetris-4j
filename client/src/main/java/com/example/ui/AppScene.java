package com.example.ui;

import com.example.state.AppState;
import javafx.scene.Node;

public interface AppScene {
    AppState getState();

    Node getNode();

    default void onEnter() {
    }

    default void onExit() {
    }
}
