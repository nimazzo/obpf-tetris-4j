package com.example.ui.game;

import com.example.state.AppState;
import com.example.state.GameState;
import com.example.ui.AppScene;
import javafx.animation.AnimationTimer;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.example.ui.TextFactory.createText;

public class GameScene extends VBox implements AppScene {

    // UI elements
    private final IntegerProperty fpsProperty = new SimpleIntegerProperty(0);

    private final List<Tetrion> tetrions = new ArrayList<>();
    private final HBox tetrionsBox;
    private final AnimationTimer animationTimer;

    // fps calculation
    private long totalFrameTime = 0;
    private int frameTimeIndex = 0;
    private long last = System.nanoTime();

    public GameScene() {
        setSpacing(10);

        tetrionsBox = new HBox(10.0);

        var fpsText = createText("FPS:", FontWeight.EXTRA_BOLD, 30, Color.WHITE, 2.0, Color.BLACK);
        var fpsCounter = createText("0", FontWeight.EXTRA_BOLD, 30, Color.WHITE, 2.0, Color.BLACK);
        var fpsBox = new HBox(10.0, fpsText, fpsCounter);
        fpsCounter.textProperty().bind(Bindings.convert(fpsProperty));
        fpsBox.setPadding(new Insets(10.0));

        getChildren().addAll(tetrionsBox, fpsBox);

        animationTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                redraw(now);
            }
        };
    }

    private void redraw(long now) {
        tetrions.forEach(Tetrion::redraw);

        totalFrameTime += now - last;
        frameTimeIndex++;
        last = now;

        if (totalFrameTime >= 1_000_000_000) {
            long averageFrameTime = totalFrameTime / frameTimeIndex;
            long fps = 1_000_000_000 / averageFrameTime;
            System.out.println("Num frames: " + frameTimeIndex + " total frame time: " + totalFrameTime + " average frame time: " + averageFrameTime);
            fpsProperty.set((int) fps);

            totalFrameTime = 0;
            frameTimeIndex = 0;
        }
    }

    @Override
    public AppState getState() {
        return AppState.GAME;
    }

    @Override
    public Node getNode() {
        return this;
    }

    @Override
    public void onEnter() {
        animationTimer.start();
    }

    @Override
    public void onExit() {
        animationTimer.stop();
    }

    public List<Tetrion> getTetrions() {
        return tetrions;
    }

    public void init() {
        var num = GameState.INSTANCE.getNumberOfPlayers();

        tetrions.clear();
        tetrionsBox.getChildren().clear();
        Stream.generate(Tetrion::new).limit(num).forEach(tetrions::add);
        tetrionsBox.getChildren().addAll(tetrions);
    }
}
