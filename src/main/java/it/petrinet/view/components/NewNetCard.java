package it.petrinet.view.components;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.input.MouseEvent; // Per gestire gli eventi del mouse

public class NewNetCard extends VBox {

    public NewNetCard() {
        this.setAlignment(Pos.CENTER);
        this.setPrefSize(150, 240); // O le dimensioni che hai deciso per le card
        this.setMaxSize(150, 240);
        this.getStyleClass().add("new-net-card"); // Assicurati che questo stile sia definito nel tuo CSS

        Label plusSymbolLabel = new Label("+");
        plusSymbolLabel.getStyleClass().add("plus-symbol"); // Stile specifico per il "+"

        Label newNetLabel = new Label("New net");
        newNetLabel.getStyleClass().add("card-title-label"); // Usa la classe di stile generica per i titoli delle card
        // o una specifica 'new-net-title-label'

        Label descriptionLabel = new Label("Create new Petri net");
        descriptionLabel.getStyleClass().add("description-label"); // Usa la classe di stile generica per le descrizioni

        this.getChildren().addAll(plusSymbolLabel, newNetLabel, descriptionLabel);

        // --- Logica di interazione (Hover e Click) ---
        this.setOnMouseEntered(e -> {
            this.setScaleX(1.02);
            this.setScaleY(1.02);
        });
        this.setOnMouseExited(e -> {
            this.setScaleX(1.0);
            this.setScaleY(1.0);
        });

        this.setOnMouseClicked(event -> {
            System.out.println("New Net Card Clicked!");
            // TODO: Implement navigation to the Petri net creation view
            // Esempio: ViewNavigator.navigateTo("CreateNetView.fxml");
        });
    }
}