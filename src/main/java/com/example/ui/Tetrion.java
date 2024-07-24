package com.example.ui;

import com.example.autogenerated.ObpfNativeInterface;
import com.example.autogenerated.ObpfTetromino;
import com.example.autogenerated.ObpfVec2;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.util.List;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class Tetrion extends StackPane {
    private static final int ROWS = ObpfNativeInterface.OBPF_MATRIX_HEIGHT();
    private static final int COLS = ObpfNativeInterface.OBPF_MATRIX_WIDTH();

    private static final int PIXELS_PER_CELL = 30;
    private static final int PADDING = 10;
    private static final Color CLEAR_COLOR = Color.rgb(175, 175, 175);
    private static final List<Color> COLORS = List.of(
            Color.rgb(0, 0, 0),
            Color.rgb(0, 240, 240),
            Color.rgb(0, 0, 240),
            Color.rgb(240, 160, 0),
            Color.rgb(240, 240, 0),
            Color.rgb(0, 240, 0),
            Color.rgb(160, 0, 240),
            Color.rgb(240, 0, 0)
    );
    private static final List<Color> GHOST_COLORS = List.of(
            Color.rgb(0, 0, 0),
            Color.rgb(0, 120, 120),
            Color.rgb(0, 0, 120),
            Color.rgb(120, 70, 0),
            Color.rgb(120, 120, 0),
            Color.rgb(0, 120, 0),
            Color.rgb(90, 0, 120),
            Color.rgb(120, 0, 0)
    );
    private final Canvas canvas;
    private final MemorySegment obpfTetrion;
    private final MemorySegment obpfMatrix;
    private final AtomicIntegerArray keysPressed = new AtomicIntegerArray(7);
    private final Text fpsCounter;
    private long last = System.nanoTime();
    private long lastSimulated = System.nanoTime();

    public Tetrion() {
        canvas = new Canvas(COLS * PIXELS_PER_CELL + PADDING * 2, ROWS * PIXELS_PER_CELL + PADDING * 2);
        fpsCounter = new Text("0");
        var debug = new HBox(5.0, new Text("FPS:"), fpsCounter);
        var vbox = new VBox(5.0, canvas, debug);

        getChildren().add(vbox);
        setPadding(new Insets(10.0));

        obpfTetrion = ObpfNativeInterface.obpf_create_tetrion(1234);
        obpfMatrix = ObpfNativeInterface.obpf_tetrion_matrix(obpfTetrion);

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                redraw(now);
            }
        }.start();
    }

    long totalFrameTime = 0;
    int frameTimeIndex = 0;

    public void redraw(long now) {
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

        // if 16 ms elapsed, simulate next frame
        if (now - lastSimulated > 16_000_000) {
            MemorySegment key_state = createKeyState();
            ObpfNativeInterface.obpf_tetrion_simulate_next_frame(obpfTetrion, key_state);
            lastSimulated = now;
        }

        drawBackground();
        drawMatrix();

        drawGhostPiece();
        drawActivePiece();
        drawGrid();
    }

    private void drawMatrix() {
        try (var arena = Arena.ofConfined()) {
            var position = ObpfVec2.allocate(arena);
            for (byte y = 0; y < ROWS; y++) {
                for (byte x = 0; x < COLS; x++) {
                    ObpfVec2.x(position, x);
                    ObpfVec2.y(position, y);
                    var type = ObpfNativeInterface.obpf_matrix_get(obpfMatrix, position);
                    if (type != 0) {
                        drawMino(x, y, type, false);
                    }
                }
            }
        }
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
            gc.strokeLine(PADDING, row * PIXELS_PER_CELL + PADDING, canvas.getWidth() - PADDING, row * PIXELS_PER_CELL + PADDING);
        }
        for (int col = 0; col <= COLS; col++) {
            gc.strokeLine(col * PIXELS_PER_CELL + PADDING, PADDING, col * PIXELS_PER_CELL + PADDING, canvas.getHeight() - PADDING);
        }
    }

    private MemorySegment createKeyState() {
        return ObpfNativeInterface.obpf_key_state_create(Arena.ofAuto(),
                keysPressed.get(0) == 1,
                keysPressed.get(1) == 1,
                keysPressed.get(2) == 1,
                keysPressed.get(3) == 1,
                keysPressed.get(4) == 1,
                keysPressed.get(5) == 1,
                keysPressed.get(6) == 1);
    }

    private void drawActivePiece() {
        try (var arena = Arena.ofConfined()) {
            var outTetromino = ObpfTetromino.allocate(arena);
            ObpfNativeInterface.obpf_tetrion_try_get_active_tetromino(obpfTetrion, outTetromino);
            drawTetromino(outTetromino, false);
        }
    }

    private void drawGhostPiece() {
        try (var arena = Arena.ofConfined()) {
            var outTetromino = ObpfTetromino.allocate(arena);
            ObpfNativeInterface.obpf_tetrion_try_get_ghost_tetromino(obpfTetrion, outTetromino);
            drawTetromino(outTetromino, true);
        }
    }

    private void drawTetromino(MemorySegment outTetromino, boolean ghostPiece) {
        var type = ObpfTetromino.type(outTetromino);
        if (type == 0) return;

        for (int i = 0; i < ObpfTetromino.mino_positions$dimensions()[0]; i++) {
            var vec2 = ObpfTetromino.mino_positions(outTetromino, i);
            int x = ObpfVec2.x(vec2);
            int y = ObpfVec2.y(vec2);
            drawMino(x, y, type, ghostPiece);
        }
    }

    public void setKeyState(int key, boolean pressed) {
        keysPressed.set(key, pressed ? 1 : 0);
    }

    private double scale(int value) {
        return PADDING + value * PIXELS_PER_CELL;
    }

    private void drawMino(int x, int y, int type, boolean ghostPiece) {
        var gc = canvas.getGraphicsContext2D();
        gc.setFill(ghostPiece ? GHOST_COLORS.get(type) : COLORS.get(type));
        gc.fillRect(scale(x), scale(y), PIXELS_PER_CELL, PIXELS_PER_CELL);
    }
}
