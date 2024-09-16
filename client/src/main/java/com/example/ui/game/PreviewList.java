package com.example.ui.game;

import com.example.simulation.Tetromino;
import javafx.geometry.Insets;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PreviewList extends VBox {

    private static final int SIZE = 6;
    private final List<PiecePreview> previewPieces = new ArrayList<>(SIZE);

    private final List<Tetromino> previewTetrominos = new ArrayList<>(SIZE);

    public PreviewList() {
        setSpacing(10);
        setBorder(new Border(new BorderStroke(Color.WHITE, BorderStrokeStyle.SOLID, CornerRadii.EMPTY, new BorderWidths(3))));
        setMaxHeight(Region.USE_PREF_SIZE);
        setBackground(Background.fill(Colors.CLEAR_COLOR.deriveColor(0, 1, 1, 0.5)));

        for (int i = 0; i < SIZE; i++) {
            previewPieces.add(new PiecePreview(15, 4, 5, 3));
            setMargin(previewPieces.get(i), new Insets(5));
            getChildren().add(previewPieces.get(i));
        }
    }

    public void update(Consumer<List<Tetromino>> updatePreviewPieces) {
        synchronized (previewTetrominos) {
            updatePreviewPieces.accept(previewTetrominos);
            for (int i = 0; i < SIZE; i++) {
                previewPieces.get(i).update(previewTetrominos.get(i));
            }
        }
    }

    public void draw() {
        synchronized (previewTetrominos) {
            for (var previewPiece : previewPieces) {
                previewPiece.draw();
            }
        }
    }
}
