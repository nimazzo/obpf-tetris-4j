package com.example.ui;

import com.example.simulation.Mino;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static com.example.ui.Colors.*;

public class PiecePreview extends StackPane {
    private static final int PIXELS_PER_CELL = 20;
    private static final int SIZE = 4;
    private static final int PADDING = 10;

    private final Canvas canvas = new Canvas(SIZE * PIXELS_PER_CELL + PADDING, SIZE * PIXELS_PER_CELL + PADDING);

    private final List<Mino> minos = new ArrayList<>(4);
    private final Rectangle boundingBox = new Rectangle(0, 0, 0, 0);

    public PiecePreview() {
        setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        setMaxHeight(Region.USE_PREF_SIZE);
        getChildren().add(canvas);
    }

    public void update(Consumer<List<Mino>> updatePreviewPiece) {
        synchronized (minos) {
            updatePreviewPiece.accept(minos);
            if (!minos.isEmpty()) {
                calculateBoundingBox();
            }
        }
    }

    public void drawMinos() {
        var gc = canvas.getGraphicsContext2D();
        gc.setFill(CLEAR_COLOR);
        gc.setStroke(Color.BLACK);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        var offsetX = (canvas.getWidth() - boundingBox.getWidth()) / 2.0 - boundingBox.getX();
        var offsetY = (canvas.getHeight() - boundingBox.getHeight()) / 2.0 - boundingBox.getY();
        synchronized (minos) {
            if (minos.isEmpty()) {
                return;
            }
            
            for (var mino : minos) {
                var color = mino.ghostPiece() ? GHOST_COLORS.get(mino.type()) : COLORS.get(mino.type());
                gc.setFill(color);
                gc.fillRect(mino.x() * PIXELS_PER_CELL + offsetX, mino.y() * PIXELS_PER_CELL + offsetY, PIXELS_PER_CELL, PIXELS_PER_CELL);
                gc.strokeRect(mino.x() * PIXELS_PER_CELL + offsetX, mino.y() * PIXELS_PER_CELL + offsetY, PIXELS_PER_CELL, PIXELS_PER_CELL);
            }
        }
    }

    private void calculateBoundingBox() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (var mino : minos) {
            minX = Math.min(minX, mino.x());
            minY = Math.min(minY, mino.y());
            maxX = Math.max(maxX, mino.x());
            maxY = Math.max(maxY, mino.y());
        }

        boundingBox.setX(minX * PIXELS_PER_CELL);
        boundingBox.setY(minY * PIXELS_PER_CELL);
        boundingBox.setWidth((maxX - minX + 1) * PIXELS_PER_CELL);
        boundingBox.setHeight((maxY - minY + 1) * PIXELS_PER_CELL);
    }
}
