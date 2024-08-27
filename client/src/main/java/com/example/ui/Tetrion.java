package com.example.ui;

import com.example.autogenerated.ObpfNativeInterface;
import com.example.simulation.Mino;
import com.example.simulation.Tetromino;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.example.ui.Colors.*;

public class Tetrion extends HBox {

    public static final int ROWS = ObpfNativeInterface.OBPF_MATRIX_HEIGHT();
    public static final int COLS = ObpfNativeInterface.OBPF_MATRIX_WIDTH();

    private static final int Y_OFFSET = ObpfNativeInterface.obpf_tetrion_num_invisible_lines();

    private static final int PIXELS_PER_CELL = 30;
    private static final int PADDING = 10;

    // UI elements
    private final Canvas canvas;
    private final Text fpsCounter = new Text();
    private final Text frameCounter = new Text();
    private final PiecePreview holdPreview = new PiecePreview();
    private final PreviewList previewList = new PreviewList();

    // fps calculation
    private long totalFrameTime = 0;
    private int frameTimeIndex = 0;
    private long last = System.nanoTime();

    private final List<Mino> gameBoard = new ArrayList<>(ROWS * COLS);

    public Tetrion() {
        canvas = new Canvas(COLS * PIXELS_PER_CELL + PADDING * 2, (ROWS - Y_OFFSET) * PIXELS_PER_CELL + PADDING * 2);
        var canvasContainer = new StackPane(canvas);
        var border = new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3)));
        canvasContainer.setBorder(border);
        var debug = new HBox(5.0, new Text("FPS:"), fpsCounter, new Text("Frame:"), frameCounter);
        var vbox = new VBox(5.0, canvasContainer, debug);
        getChildren().addAll(holdPreview, vbox, previewList);
        setSpacing(10.0);
        setPadding(new Insets(10.0));

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                redraw(now);
            }
        }.start();
    }

    public void update(Consumer<List<Mino>> updateGameBoard) {
        synchronized (gameBoard) {
            updateGameBoard.accept(gameBoard);
        }
    }

    public void updateHoldPiece(Tetromino holdPiece) {
        holdPreview.update(holdPiece);
    }

    public void updatePreviewPieces(Consumer<List<Tetromino>> updatePreviewPieces) {
        previewList.update(updatePreviewPieces);
    }

    private void redraw(long now) {
        totalFrameTime += now - last;
        frameTimeIndex++;
        last = now;

        if (totalFrameTime >= 1_000_000_000) {
            long averageFrameTime = totalFrameTime / frameTimeIndex;
            long fps = 1_000_000_000 / averageFrameTime;
            fpsCounter.setText(String.format("%d", fps));

            totalFrameTime = 0;
            frameTimeIndex = 0;
        }

        drawBackground();
        drawMinos();
        drawGrid();
        holdPreview.draw();
        previewList.draw();
    }

    private void drawBackground() {
        var gc = canvas.getGraphicsContext2D();
        gc.setFill(CLEAR_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawGrid() {
        var gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);

        for (int row = 0; row <= ROWS; row++) {
            gc.strokeLine(PADDING, row * PIXELS_PER_CELL + PADDING, canvas.getWidth() - PADDING, row * PIXELS_PER_CELL + PADDING);
        }
        for (int col = 0; col <= COLS; col++) {
            gc.strokeLine(col * PIXELS_PER_CELL + PADDING, PADDING, col * PIXELS_PER_CELL + PADDING, canvas.getHeight() - PADDING);
        }
    }

    private void drawMinos() {
        synchronized (gameBoard) {
            for (var mino : gameBoard) {
                drawMino(mino.x(), mino.y() - Y_OFFSET, mino.type(), mino.ghostPiece());
            }
        }
    }

    private void drawMino(int x, int y, int type, boolean ghostPiece) {
        var gc = canvas.getGraphicsContext2D();
        gc.setFill(ghostPiece ? GHOST_COLORS.get(type) : COLORS.get(type));
        gc.fillRect(scale(x), scale(y), PIXELS_PER_CELL, PIXELS_PER_CELL);
    }

    private double scale(int value) {
        return PADDING + value * PIXELS_PER_CELL;
    }

    public void setCurrentFrame(long frame) {
        Platform.runLater(() -> frameCounter.setText("" + frame));
    }
}
