package com.example.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TextFactory {
    private TextFactory() {
    }

    public static Text createText(String text, FontWeight fontWeight, int fontSize, Color color, double outlineWidth, Color outlineColor) {
        var textNode = new Text(text);
        var font = Font.font("Arial Black", fontWeight, fontSize);
        textNode.setFont(font);

        textNode.setFill(color);
        textNode.setStroke(outlineColor);
        textNode.setStrokeWidth(outlineWidth);

        return textNode;
    }
}
