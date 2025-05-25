package it.petrinet.view.components;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class Card extends StackPane {

    public Card(String name, Color backgroundColor) {
        Rectangle background = new Rectangle(100, 150);
        background.setFill(backgroundColor);
        background.setArcWidth(15);
        background.setArcHeight(15);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14; -fx-font-weight: bold;");

        this.getChildren().addAll(background, nameLabel);
        this.setStyle("-fx-padding: 10; -fx-alignment: top-center;");
    }
}
