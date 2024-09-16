package com.example.ui.game;

import com.example.simulation.Stats;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import static com.example.ui.TextFactory.createText;
import static com.example.ui.game.Colors.CLEAR_COLOR;

public class StatsPanel extends GridPane {

    private final Property<Stats> statsProperty = new SimpleObjectProperty<>(new Stats(0, 0, 0, 0));

    public StatsPanel() {
        setHgap(10);
        setVgap(10);
        setPadding(new Insets(10, 10, 10, 10));
        setBackground(new Background(new BackgroundFill(CLEAR_COLOR.deriveColor(0, 1, 1, 0.8), new CornerRadii(25), null)));
        setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, new CornerRadii(25), new BorderWidths(5))));

        Text levelText = createText("Level:", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        add(levelText, 0, 0);

        Text scoreText = createText("Score:", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        add(scoreText, 0, 1);

        Text linesText = createText("Lines:", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        add(linesText, 0, 2);

        Text timerText = createText("Time:", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        add(timerText, 0, 3);

        Text levelValue = createText("0", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        levelValue.textProperty().bind(Bindings.createStringBinding(() -> "" + statsProperty.getValue().level(), statsProperty));
        add(levelValue, 1, 0);

        Text scoreValue = createText("0", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        scoreValue.textProperty().bind(Bindings.createStringBinding(() -> "" + statsProperty.getValue().score(), statsProperty));
        add(scoreValue, 1, 1);

        Text linesValue = createText("0", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        linesValue.textProperty().bind(Bindings.createStringBinding(() -> "" + statsProperty.getValue().linesCleared(), statsProperty));
        add(linesValue, 1, 2);

        Text timerValue = createText("0", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        timerValue.textProperty().bind(Bindings.createStringBinding(() -> formatTime(statsProperty.getValue().time()), statsProperty));
        add(timerValue, 1, 3);
    }

    private String formatTime(long ms) {
        var minutes = ms / 60000;
        var seconds = (ms % 60000) / 1000;
        var deciSeconds = (ms % 1000) / 100;
        return String.format("%02d:%02d.%01d", minutes, seconds, deciSeconds);
    }

    public void update(Stats stats) {
        Platform.runLater(() -> statsProperty.setValue(stats));
    }
}
