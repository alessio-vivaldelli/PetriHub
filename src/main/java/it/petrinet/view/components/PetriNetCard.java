package it.petrinet.view.components;

import javafx.geometry.Pos; // Used for alignment within the StackPane if needed
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public class PetriNetCard extends StackPane {

    // The constructor now takes the parameters expected by HomeController
    // but we'll adapt it to display "Test Card" on a white background.
    public PetriNetCard(String title, String description, String imagePath) {
        // Create the background rectangle with YOUR dimensions (100, 150)
        Rectangle background = new Rectangle(100, 150);
        background.setFill(Color.WHITE); // Set background to white
        background.setArcWidth(15);
        background.setArcHeight(15);
        // Add a subtle border to define the card
        background.setStyle("-fx-stroke: #ddd; -fx-stroke-width: 1;");

        // Create the Label for the text "Test Card"
        Label testLabel = new Label("Test Card");
        // Set text color to black for visibility on white background
        testLabel.setStyle("-fx-text-fill: black; -fx-font-size: 14px; -fx-font-weight: bold;");

        // Add the background rectangle and the label to the StackPane
        this.getChildren().addAll(background, testLabel);

        // Apply StackPane styling from your example
        // (Note: -fx-alignment: top-center aligns children within the stackpane)
        this.setStyle("-fx-padding: 10; -fx-alignment: center;"); // Changed to center for test label

        // Optional: Add hover effect for better UX
        this.setOnMouseEntered(e -> {
            background.setStyle("-fx-stroke: #bbb; -fx-stroke-width: 2; -fx-cursor: hand;"); // Darker border on hover
            this.setScaleX(1.05); // Slightly larger scale for a more noticeable effect
            this.setScaleY(1.05);
        });
        this.setOnMouseExited(e -> {
            background.setStyle("-fx-stroke: #ddd; -fx-stroke-width: 1;"); // Original border on exit
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });

        // Add click action
        this.setOnMouseClicked(event -> {
            System.out.println("Test Card clicked!");
            // You can use the 'title' parameter here if you re-enable it for display
            // System.out.println("Card clicked: " + title);
        });
    }
}