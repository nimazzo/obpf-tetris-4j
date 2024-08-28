package com.example.ui;

import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class TextFactory {
    private TextFactory() {
    }

    public static Text createText(String text, FontWeight fontWeight, int fontSize, Color color, double outlineWidth, Color outlineColor) {
        var fpsText = new Text(text);
        var font = Font.font("Arial Black", fontWeight, fontSize);
        fpsText.setFont(font);

        fpsText.setFill(color);
        fpsText.setStroke(outlineColor);
        fpsText.setStrokeWidth(outlineWidth);
        
        return fpsText;
    }
}
