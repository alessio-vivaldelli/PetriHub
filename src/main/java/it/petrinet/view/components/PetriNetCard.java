package it.petrinet.view.components;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox; // Potrebbe non servire qui, ma lo lascio per completezza
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.paint.Color; // Potrebbe non servire qui, ma lo lascio per completezza
import javafx.scene.text.Font; // Potrebbe non servire qui, ma lo lascio per completezza

public class PetriNetCard extends VBox {

    private static final String DEFAULT_CARD_IMAGE_PATH = "/assets/background/dummy.png";
    private static final String PATH = "/assets/background/";

    private StackPane imageContainer;
    private ImageView backgroundImageView;
    private Label titleLabel;
    private Label descriptionLabel;
    private StackPane greenButtonOverlay; // Il pulsante verde in overlay

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
            String actualImagePath = (imagePath != null && !imagePath.isEmpty()) ? PATH + imagePath : DEFAULT_CARD_IMAGE_PATH;
            Image cardImage = new Image(getClass().getResourceAsStream(actualImagePath));
            backgroundImageView.setImage(cardImage);
            backgroundImageView.setFitWidth(240);
            backgroundImageView.setFitHeight(179);
            backgroundImageView.setPreserveRatio(false);

            Rectangle clip = new Rectangle(
                    backgroundImageView.getFitWidth(),
                    backgroundImageView.getFitHeight()
            );
            clip.setArcWidth(12);
            clip.setArcHeight(12);
            backgroundImageView.setClip(clip);
            backgroundImageView.getStyleClass().add("card-image-view");
        } catch (Exception e) {
            System.err.println("Error loading card image: " + e.getMessage());
            imageContainer.setStyle("-fx-background-color: #313244; -fx-background-radius: 8px;");
        }
        imageContainer.getChildren().add(backgroundImageView);
        StackPane.setAlignment(backgroundImageView, Pos.CENTER);

        // --- 2. Green Button Overlay (NASCOSTO DI DEFAULT) ---
        greenButtonOverlay = new StackPane();
        greenButtonOverlay.getStyleClass().add("green-button-overlay");
        greenButtonOverlay.setPrefSize(25, 25); // Dimensione del pulsante
        greenButtonOverlay.setMaxSize(25, 25);

        // IMPOSTA INIZIALMENTE A NASCOSTO E NON GESTITO
        greenButtonOverlay.setVisible(false);
        greenButtonOverlay.setManaged(false); // Non deve occupare spazio quando nascosto

        Circle buttonCircle = new Circle(20); // Raggio del cerchio
        buttonCircle.getStyleClass().add("green-button-circle");
        greenButtonOverlay.getChildren().add(buttonCircle);

        Label buttonIcon = new Label("☰"); // Icona Unicode
        buttonIcon.getStyleClass().add("green-button-icon");
        greenButtonOverlay.getChildren().add(buttonIcon);
        StackPane.setAlignment(buttonIcon, Pos.CENTER);

        StackPane.setAlignment(greenButtonOverlay, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(greenButtonOverlay, new Insets(0, 10, 10, 0));

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


        // --- LOGICA DI HOVER PER MOSTRARE/NASCONDERE IL PULSANTE ---
        this.setOnMouseEntered(event -> {
            // Mostra il pulsante quando il mouse entra nella card
            greenButtonOverlay.setVisible(true);
            greenButtonOverlay.setManaged(true);
            // Puoi aggiungere un effetto di scala anche per la card intera qui, se non lo fai già in CSS
            this.setScaleX(1.02);
            this.setScaleY(1.02);
        });

        this.setOnMouseExited(event -> {
            // Nasconde il pulsante quando il mouse esce dalla card
            greenButtonOverlay.setVisible(false);
            greenButtonOverlay.setManaged(false);
            // Ripristina l'effetto di scala della card
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });
        // --- FINE LOGICA DI HOVER ---


        // Il click della card intera (attento a non sovrapporlo al click del bottone)
        this.setOnMouseClicked(event -> {
            // Se l'evento non è stato consumato dal bottone, allora è un click sulla card
            if (!event.isConsumed()) {
                System.out.println("PetriNet Card clicked: " + title);
            }
        });

        // Il click specifico del bottone verde
        greenButtonOverlay.setOnMouseClicked(event -> {
            System.out.println("Green button clicked for: " + title);
            event.consume(); // FONDAMENTALE: Consuma l'evento per impedire che la card riceva il click
        });
    }
}