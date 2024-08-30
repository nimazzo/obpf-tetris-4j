package com.example.ui.menu;

import com.example.ui.Colors;
import com.example.ui.TextFactory;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class MainMenu extends StackPane {

    private final Button singlePlayerButton;
    private final Button multiPlayerButton;

    public MainMenu() {
        setPrefSize(750, 600);

        var content = new VBox(10);

        content.setAlignment(Pos.TOP_CENTER);
        content.setBackground((Background.fill(Colors.CLEAR_COLOR.deriveColor(0, 1, 1, 0.6))));
        content.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(0, 4, 0, 4))));

        content.setPadding(new Insets(40));
        content.setPrefWidth(400);

        content.setMaxWidth(USE_PREF_SIZE);

        var title = TextFactory.createText("TetrisJ", FontWeight.BOLD, 64, Color.ORANGE, 4, Color.BLACK);
        VBox.setMargin(title, new Insets(0, 0, 20, 0));

        singlePlayerButton = new Button("Single Player");
        singlePlayerButton.setFont(Font.font(15));

        multiPlayerButton = new Button("Multi Player");
        multiPlayerButton.setFont(Font.font(15));

        var buttonBox = new VBox(15, singlePlayerButton, multiPlayerButton);
        buttonBox.setPrefWidth(200);
        buttonBox.setMaxWidth(USE_PREF_SIZE);
        singlePlayerButton.setMaxWidth(Double.MAX_VALUE);
        multiPlayerButton.setMaxWidth(Double.MAX_VALUE);
        buttonBox.setAlignment(Pos.TOP_CENTER);

        content.getChildren().addAll(title, buttonBox);

        getChildren().add(content);
    }

    public void setOnSinglePlayerButtonClicked(Runnable action) {
        singlePlayerButton.setOnAction(_ -> action.run());
    }

    public void setOnMultiPlayerButtonClicked(Runnable action) {
        multiPlayerButton.setOnAction(_ -> action.run());
    }
}
