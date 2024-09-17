package com.example.ui.views.menu;

import com.example.state.AppState;
import com.example.ui.AppScene;
import com.example.ui.TextFactory;
import com.example.ui.views.game.Colors;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class GameMenu extends StackPane implements AppScene {

    private final Button returnToGameButton;
    private final Button leaveGameButton;

    public GameMenu() {
        setPrefSize(1000, 700);

        var content = new VBox(10);

        content.setAlignment(Pos.TOP_CENTER);
        content.setBackground((Background.fill(Colors.CLEAR_COLOR.deriveColor(0, 1, 1, 0.6))));
        content.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 4, 0, 4))));

        content.setPadding(new Insets(40));
        content.setPrefWidth(400);

        content.setMaxWidth(USE_PREF_SIZE);

        var title = TextFactory.createText("Game Menu", FontWeight.BOLD, 64, Color.ORANGE, 4, Color.BLACK);
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        returnToGameButton = new Button("Return to Game");
        returnToGameButton.setFont(Font.font(15));

        leaveGameButton = new Button("Leave Game");
        leaveGameButton.setFont(Font.font(15));

        var buttonBox = new VBox(15, returnToGameButton, leaveGameButton);
        buttonBox.setPrefWidth(200);
        buttonBox.setMaxWidth(USE_PREF_SIZE);
        returnToGameButton.setMaxWidth(Double.MAX_VALUE);
        leaveGameButton.setMaxWidth(Double.MAX_VALUE);
        buttonBox.setAlignment(Pos.TOP_CENTER);

        content.getChildren().addAll(title, buttonBox);

        getChildren().add(content);
    }

    public void setOnReturnToGameButtonClicked(Runnable action) {
        returnToGameButton.setOnAction(_ -> action.run());
    }

    public void setOnLeaveGameButtonClicked(Runnable action) {
        leaveGameButton.setOnAction(_ -> action.run());
    }

    @Override
    public AppState getState() {
        return AppState.GAME_MENU;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void onEnter() {
        returnToGameButton.requestFocus();
    }
}
