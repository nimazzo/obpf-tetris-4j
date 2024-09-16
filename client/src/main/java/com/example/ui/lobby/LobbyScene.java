package com.example.ui.lobby;

import com.example.state.AppState;
import com.example.ui.AppScene;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Separator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.springframework.web.client.RestClient;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;

public class LobbyScene extends AnchorPane implements AppScene {

    private final Button updateButton;
    private final Button joinButton;
    private final Button createButton;

    private final TableView<Lobby> table;
    private final ObservableList<Lobby> lobbies = FXCollections.observableArrayList();

    public LobbyScene() {
        setPadding(new Insets(20));

        table = new TableView<>(lobbies);
        var nameCol = new TableColumn<Lobby, String>("Lobby Name");
        nameCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().name()));
        table.getColumns().add(nameCol);
        var ownerCol = new TableColumn<Lobby, String>("Owner");
        ownerCol.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().owner()));
        table.getColumns().add(ownerCol);
        var playerCol = new TableColumn<Lobby, Number>("Players");
        playerCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().numberOfPlayers()));
        table.getColumns().add(playerCol);
        var maxPlayerCol = new TableColumn<Lobby, Number>("Max Players");
        maxPlayerCol.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().maxPlayers()));
        table.getColumns().add(maxPlayerCol);

        updateButton = new Button("Update");
        var separator = new Separator(Orientation.HORIZONTAL);
        separator.setVisible(false);
        joinButton = new Button("Join Lobby");
        createButton = new Button("Create New Lobby");
        var hbox = new HBox(10.0, updateButton, separator, joinButton, createButton);
        hbox.setPadding(new Insets(7, 0, 7, 0));
        hbox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(separator, Priority.ALWAYS);

        var vbox = new VBox(table, hbox);

        AnchorPane.setTopAnchor(vbox, 25.0);
        AnchorPane.setLeftAnchor(vbox, 25.0);
        AnchorPane.setRightAnchor(vbox, 25.0);
        AnchorPane.setBottomAnchor(vbox, 0.0);

        setOnUpdateButtonClicked(this::fetchLobbiesFromServer);
        setOnJoinLobbyButtonClicked(this::joinLobby);

        getChildren().add(vbox);
    }

    private void joinLobby() {
        var selectedLobby = table.getSelectionModel().getSelectedItem();
        if (selectedLobby != null) {
            System.out.println("Joining lobby: " + selectedLobby);
        }
    }

    private void fetchLobbiesFromServer() {
        var restClient = RestClient.create("http://localhost:8080/lobby");
        var encodedAuth = Base64.getEncoder().encodeToString("user:user".getBytes(StandardCharsets.UTF_8));
        ObjectMapper objectMapper = new ObjectMapper();

        var response = restClient
                .get()
                .header("Authorization", "Basic " + encodedAuth)
                .header("Content-Type", "application/ json")
                .retrieve()
                .toEntity(String.class);
        try {
            var jsonTree = objectMapper.readTree(response.getBody());
            var content = jsonTree.get("content");
            List<Lobby> lobbies = objectMapper.readValue(content.toString(), new TypeReference<>() {
            });
            setLobbies(lobbies);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void setOnUpdateButtonClicked(Runnable action) {
        updateButton.setOnAction(_ -> action.run());
    }

    public void setOnCreateLobbyButtonClicked(Runnable action) {
        createButton.setOnAction(_ -> action.run());
    }

    public void setOnJoinLobbyButtonClicked(Runnable action) {
        joinButton.setOnAction(_ -> action.run());
    }

    public void setLobbies(List<Lobby> lobbies) {
        System.out.println("setLobbies: " + lobbies);
        this.lobbies.setAll(lobbies);
    }

    @Override
    public AppState getState() {
        return AppState.LOBBIES;
    }

    @Override
    public Node getNode() {
        return this;
    }
}
