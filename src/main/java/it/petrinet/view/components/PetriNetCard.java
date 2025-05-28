package it.petrinet.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle; // <--- Import Rectangle for clipping
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class PetriNetCard extends VBox {

    private static final String DEFAULT_CARD_IMAGE_PATH = "/assets/background/dummy.png";
    private static final String DEFAULT_BUTTON_ICON_PATH = "/assets/icons/menu.png";

    private StackPane imageContainer;
    private ImageView backgroundImageView;
    private Label titleLabel;
    private Label descriptionLabel;
    private StackPane greenButtonOverlay;

    public PetriNetCard(String title, String description, String imagePath) {
        this.getStyleClass().add("petri-net-card");
        this.setAlignment(Pos.TOP_LEFT);
        this.setSpacing(5);
        this.setPadding(new Insets(10));

        // --- 1. Image Container (StackPane for image + overlay button) ---
        imageContainer = new StackPane();
        imageContainer.getStyleClass().add("card-image-container");
        imageContainer.setPrefSize(240, 179);
        imageContainer.setMaxSize(240, 179);

        backgroundImageView = new ImageView();
        try {
            String actualImagePath = (imagePath != null && !imagePath.isEmpty()) ? imagePath : DEFAULT_CARD_IMAGE_PATH;
            Image cardImage = new Image(getClass().getResourceAsStream(actualImagePath));
            backgroundImageView.setImage(cardImage);
            backgroundImageView.setFitWidth(240);
            backgroundImageView.setFitHeight(179);
            backgroundImageView.setPreserveRatio(false); // This ensures it fills and crops

            // --- ADD THIS CLIP CODE FOR ROUNDED IMAGE CORNERS ---
            Rectangle clip = new Rectangle(
                    backgroundImageView.getFitWidth(),
                    backgroundImageView.getFitHeight()
            );
            // Match the radius used in your CSS for .card-image-container or adjust as needed
            clip.setArcWidth(12); // Arc width for horizontal rounding (2 * border-radius)
            clip.setArcHeight(12); // Arc height for vertical rounding (2 * border-radius)
            backgroundImageView.setClip(clip);
            // -----------------------------------------------------

            backgroundImageView.getStyleClass().add("card-image-view");
        } catch (Exception e) {
            System.err.println("Error loading card image: " + e.getMessage());
            imageContainer.setStyle("-fx-background-color: #313244; -fx-background-radius: 8px;");
        }
        imageContainer.getChildren().add(backgroundImageView);
        StackPane.setAlignment(backgroundImageView, Pos.CENTER);

        // ... (rest of your PetriNetCard code, including green button overlay and text labels) ...

        // --- 2. Green Button Overlay (initially hidden) ---
        greenButtonOverlay = new StackPane();
        greenButtonOverlay.getStyleClass().add("green-button-overlay");
        greenButtonOverlay.setPrefSize(28, 28);
        greenButtonOverlay.setMaxSize(28, 28);
        greenButtonOverlay.setVisible(false);
        greenButtonOverlay.setManaged(false);

        Circle buttonCircle = new Circle(14);
        buttonCircle.getStyleClass().add("green-button-circle");
        greenButtonOverlay.getChildren().add(buttonCircle);

        Label buttonIcon = new Label(">");
        buttonIcon.getStyleClass().add("green-button-icon");
        greenButtonOverlay.getChildren().add(buttonIcon);
        StackPane.setAlignment(buttonIcon, Pos.CENTER);

        StackPane.setAlignment(greenButtonOverlay, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(greenButtonOverlay, new Insets(0, 5, 5, 0));

        imageContainer.getChildren().add(greenButtonOverlay);


        // --- 3. Text Labels ---
        titleLabel = new Label(title);
        titleLabel.getStyleClass().add("card-title-label");
        titleLabel.setWrapText(true);
        VBox.setMargin(titleLabel, new Insets(5, 0, 0, 0));

        descriptionLabel = new Label(description);
        descriptionLabel.getStyleClass().add("description-label");
        descriptionLabel.setWrapText(true);


        this.getChildren().addAll(imageContainer, titleLabel, descriptionLabel);

        // --- Interaction: Hover and Click ---
        this.setOnMouseEntered(e -> {
            this.setScaleX(1.02);
            this.setScaleY(1.02);
            greenButtonOverlay.setVisible(true);
            greenButtonOverlay.setManaged(true);
        });
        this.setOnMouseExited(e -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
            greenButtonOverlay.setVisible(false);
            greenButtonOverlay.setManaged(false);
        });

        this.setOnMouseClicked(event -> {
            System.out.println("PetriNet Card clicked: " + title);
        });

        greenButtonOverlay.setOnMouseClicked(event -> {
            System.out.println("Green button clicked for: " + title);
            event.consume();
        });
    }



}