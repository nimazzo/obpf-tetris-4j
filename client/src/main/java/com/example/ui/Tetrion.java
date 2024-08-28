package com.example.ui;

import com.example.autogenerated.ObpfNativeInterface;
import com.example.simulation.Mino;
import com.example.simulation.Tetromino;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.example.ui.Colors.*;
import static com.example.ui.TextFactory.createText;

public class Tetrion extends HBox {

    public static final int ROWS = ObpfNativeInterface.OBPF_MATRIX_HEIGHT();
    public static final int COLS = ObpfNativeInterface.OBPF_MATRIX_WIDTH();

    private static final int Y_OFFSET = ObpfNativeInterface.obpf_tetrion_num_invisible_lines();

    private static final int PIXELS_PER_CELL = 30;

    // UI elements
    private final Canvas canvas;
    private final StackPane canvasContainer = new StackPane();
    private final Text frameCounter = createText("", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
    private final PiecePreview holdPreview = new PiecePreview();
    private final PreviewList previewList = new PreviewList();

    private final List<Mino> gameBoard = new ArrayList<>(ROWS * COLS);

    private boolean isGameOver = false;

    public Tetrion() {
        canvas = new Canvas(COLS * PIXELS_PER_CELL, (ROWS - Y_OFFSET) * PIXELS_PER_CELL);
        canvasContainer.getChildren().add(canvas);
        var border = new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(5)));
        canvasContainer.setBorder(border);
        var frameText = createText("Frame:", FontWeight.BOLD, 20, Color.WHITE, 1.0, Color.BLACK);
        var debug = new HBox(5.0, frameText, frameCounter);
        var vbox = new VBox(5.0, canvasContainer, debug);
        getChildren().addAll(holdPreview, vbox, previewList);
        setSpacing(10.0);
        setPadding(new Insets(10.0));
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

    public void redraw() {
        if (isGameOver) {
            return;
        }

        drawBackground();
        drawMinos();
        drawGrid();
        holdPreview.draw();
        previewList.draw();
    }

    private void drawBackground() {
        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setFill(CLEAR_COLOR);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    private void drawGrid() {
        var gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.BLACK);

        for (int row = 0; row <= ROWS; row++) {
            gc.strokeLine(0, row * PIXELS_PER_CELL, canvas.getWidth(), row * PIXELS_PER_CELL);
        }
        for (int col = 0; col <= COLS; col++) {
            gc.strokeLine(col * PIXELS_PER_CELL, 0, col * PIXELS_PER_CELL, canvas.getHeight());
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
        return value * PIXELS_PER_CELL;
    }

    public void setCurrentFrame(long frame) {
        Platform.runLater(() -> frameCounter.setText("" + frame));
    }

    public void setGameOver() {
        if (!isGameOver) {
            isGameOver = true;
            var greyScreen = new Pane();
            greyScreen.setBackground(Background.fill(Color.rgb(0, 0, 0, 0.5)));
            greyScreen.setPrefSize(canvasContainer.getWidth(), canvasContainer.getHeight());

            var gameOverMessage = new Text("Game Over");
            gameOverMessage.setFill(Color.RED);
            var font = Font.font("Arial Black", FontWeight.EXTRA_BOLD, 45);
            gameOverMessage.setFont(font);
            gameOverMessage.setStroke(Color.BLACK);
            gameOverMessage.setStrokeWidth(3);
            Platform.runLater(() -> canvasContainer.getChildren().addAll(greyScreen, gameOverMessage));
        }
    }
}
