package com.example.ui;

import com.example.simulation.Tetromino;
import javafx.scene.canvas.Canvas;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.List;

import static com.example.ui.Colors.COLORS;
import static com.example.ui.Colors.GHOST_COLORS;

public class PiecePreview extends StackPane {
    private static final int DEFAULT_PIXELS_PER_CELL = 20;
    private static final int DEFAULT_SIZE = 4;
    private static final int DEFAULT_PADDING = 5;
    private static final double DEFAULT_BORDER_WIDTH = 5;
    private final int pixelsPerCell;
    private final Canvas canvas;

    private final Rectangle boundingBox = new Rectangle(0, 0, 0, 0);
    private volatile Tetromino holdPiece = new Tetromino(List.of());

    public PiecePreview() {
        this(DEFAULT_PIXELS_PER_CELL, DEFAULT_SIZE, DEFAULT_PADDING, DEFAULT_BORDER_WIDTH);
    }

    public PiecePreview(int pixelsPerCell, int size, int padding, double borderWidth) {
        this.pixelsPerCell = pixelsPerCell;

        canvas = new Canvas(size * pixelsPerCell + padding, size * pixelsPerCell + padding);
        setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(borderWidth))));
        setMaxHeight(Region.USE_PREF_SIZE);
        setBackground(Background.fill(Colors.CLEAR_COLOR.deriveColor(0, 1, 1, 0.5)));
        getChildren().add(canvas);
    }

    public void update(Tetromino holdPiece) {
        this.holdPiece = holdPiece;
        calculateBoundingBox();
    }

    public void draw() {
        var gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        var offsetX = (canvas.getWidth() - boundingBox.getWidth()) / 2.0 - boundingBox.getX();
        var offsetY = (canvas.getHeight() - boundingBox.getHeight()) / 2.0 - boundingBox.getY();

        for (var mino : holdPiece.minos()) {
            var color = mino.ghostPiece() ? GHOST_COLORS.get(mino.type()) : COLORS.get(mino.type());
            gc.setFill(color);
            gc.fillRect(mino.x() * pixelsPerCell + offsetX, mino.y() * pixelsPerCell + offsetY, pixelsPerCell, pixelsPerCell);
            gc.strokeRect(mino.x() * pixelsPerCell + offsetX, mino.y() * pixelsPerCell + offsetY, pixelsPerCell, pixelsPerCell);
        }
    }

    private void calculateBoundingBox() {
        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;
        double maxY = Double.MIN_VALUE;

        for (var mino : holdPiece.minos()) {
            minX = Math.min(minX, mino.x());
            minY = Math.min(minY, mino.y());
            maxX = Math.max(maxX, mino.x());
            maxY = Math.max(maxY, mino.y());
        }

        boundingBox.setX(minX * pixelsPerCell);
        boundingBox.setY(minY * pixelsPerCell);
        boundingBox.setWidth((maxX - minX + 1) * pixelsPerCell);
        boundingBox.setHeight((maxY - minY + 1) * pixelsPerCell);
    }
}
