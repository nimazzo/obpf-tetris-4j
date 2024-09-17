package com.example.ui.views.game;

import com.example.daos.LobbyCreationRequest;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;

public class CreateLobbyDialog extends Dialog<LobbyCreationRequest> {

    private final TextField lobbyNameInput = new TextField();
    private final TextField maxPlayersInput = new TextField();

    public CreateLobbyDialog() {
        setTitle("Lobby Creation");
        setHeaderText("Enter the lobby details");

        var createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        grid.add(new Label("Lobby Name:"), 0, 0);
        grid.add(lobbyNameInput, 1, 0);
        grid.add(new Label("Max Players:"), 0, 1);
        grid.add(maxPlayersInput, 1, 1);

        var createButton = getDialogPane().lookupButton(createButtonType);
        createButton.setDisable(true);

        lobbyNameInput.textProperty().addListener((_, _, _) ->
                createButton.setDisable(invalidInputs()));

        maxPlayersInput.textProperty().addListener((_, _, _) ->
                createButton.setDisable(invalidInputs()));

        getDialogPane().setContent(grid);

        lobbyNameInput.requestFocus();

        setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new LobbyCreationRequest(lobbyNameInput.getText(), Integer.parseInt(maxPlayersInput.getText()));
            }
            return null;
        });
    }

    private boolean invalidInputs() {
        var lobbyName = lobbyNameInput.getText();
        var maxPlayers = maxPlayersInput.getText();

        boolean isValidNumber;
        try {
            var value = Integer.parseInt(maxPlayers);
            isValidNumber = (value >= 1 && value <= 6);
        } catch (NumberFormatException e) {
            isValidNumber = false;
        }

        return lobbyName.isBlank() || maxPlayers.isBlank() || !isValidNumber;
    }
}
