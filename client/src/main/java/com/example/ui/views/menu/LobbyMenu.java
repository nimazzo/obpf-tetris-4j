package com.example.ui.views.menu;

import com.example.state.AppState;
import com.example.ui.AppScene;
import com.example.ui.TextFactory;
import com.example.ui.views.game.Colors;
import com.example.ui.views.game.Lobby;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
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
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.util.List;
import java.util.function.Function;

public class LobbyMenu extends StackPane implements AppScene {

    private final Button backToMainMenu;
    private final Button joinButton;
    private final Button createButton;
    private final Button updateButton;

    private final TableView<Lobby> table;
    private final ObservableList<Lobby> lobbies = FXCollections.observableArrayList();

    public LobbyMenu() {
        setPrefSize(1000, 700);
        setPadding(new Insets(0, 120, 0, 120));

        var content = new VBox();
        content.setAlignment(Pos.TOP_CENTER);
        content.setBackground((Background.fill(Colors.CLEAR_COLOR.deriveColor(0, 1, 1, 0.6))));
        content.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 4, 0, 4))));

        var title = TextFactory.createText("Lobby Menu", FontWeight.BOLD, 64, Color.ORANGE, 4, Color.BLACK);
        VBox.setMargin(title, new Insets(40, 0, 0, 0));

        table = createTable();

        backToMainMenu = new Button("Return to Main Menu");
        var separator = new Separator(Orientation.HORIZONTAL);
        separator.setVisible(false);
        joinButton = new Button("Join Lobby");
        createButton = new Button("Create New Lobby");
        updateButton = new Button("Update");
        var hbox = new HBox(10, backToMainMenu, separator, joinButton, createButton);
        hbox.setPadding(new Insets(7, 0, 7, 0));
        hbox.setAlignment(Pos.CENTER_RIGHT);
        HBox.setHgrow(separator, Priority.ALWAYS);

        var updateButtonBox = new HBox(updateButton);
        updateButtonBox.setAlignment(Pos.CENTER_RIGHT);
        var vbox = new VBox(10, updateButtonBox, table, hbox);
        vbox.setPadding(new Insets(20, 25, 40, 25));

        content.getChildren().addAll(title, vbox);
        getChildren().addAll(content);
        VBox.setVgrow(table, Priority.ALWAYS);
        VBox.setVgrow(vbox, Priority.ALWAYS);
    }

    private TableView<Lobby> createTable() {
        var table = new TableView<>(lobbies);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        var nameCol = createTableColumn("Name", 0.35, table, Lobby::name);
        var ownerCol = createTableColumn("Owner", 0.25, table, Lobby::owner);
        var playerCol = createTableColumn("Players", 0.2, table, Lobby::numberOfPlayers);
        var maxPlayerCol = createTableColumn("Max Players", 0.2, table, Lobby::maxPlayers);
        table.getColumns().addAll(List.of(nameCol, ownerCol, playerCol, maxPlayerCol));
        return table;
    }

    private static TableColumn<Lobby, ?> createTableColumn(String name, double width, TableView<Lobby> table,
                                                           Function<Lobby, Object> clb) {
        var column = new TableColumn<Lobby, Object>(name);
        column.setCellValueFactory(cellData -> new SimpleObjectProperty<>(clb.apply(cellData.getValue())));
        column.setResizable(false);
        column.prefWidthProperty().bind(table.widthProperty().multiply(width));
        column.setReorderable(false);
        return column;
    }

    public void setOnReturnToMainMenuButtonClicked(Runnable action) {
        backToMainMenu.setOnAction(_ -> action.run());
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
        Platform.runLater(() -> this.lobbies.setAll(lobbies));
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
