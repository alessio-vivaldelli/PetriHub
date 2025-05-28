package it.petrinet.controller;

import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.PetriNetCard;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import java.io.InputStream;

public class HomeController {

    private User user;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label myNets;
    @FXML
    private Label subNets;
    @FXML
    private Label discorver;

    @FXML
    private HBox myNetsList;
    @FXML
    private VBox myNetsSection;

    @FXML
    private HBox subNetsList;
    @FXML
    private VBox subNetsSection;

    @FXML
    private HBox discoverList;
    @FXML
    private VBox discoverSection;

    @FXML
    private void initialize() {
        this.user = ViewNavigator.getAuthenticatedUser();
        userNameLabel.setText("welcome, " + user.getUsername());

        if (subNetsSection != null) subNetsSection.setVisible(false);
        if (myNetsSection != null) myNetsSection.setVisible(false);
        if (discoverSection != null) discoverSection.setVisible(false);

        if (myNetsList != null) myNetsList.getChildren().clear();
        if (subNetsList != null) subNetsList.getChildren().clear();
        if (discoverList != null) discoverList.getChildren().clear();

        // Aggiungi le icone
        addIconsToLabels("subNets.png", subNets);
        addIconsToLabels("myNets.png", myNets);
        addIconsToLabels("discover.png", discorver);

        if (user != null /* && user.isAdmin() */) {
            setAllHomeSections();
        } else {
            setUserHome();
        }
    }

    private void addIconsToLabels(String iconName, Label label) {
        // Percorso base per le risorse
        String basePath = "/assets/icons/"; // Assicurati che questo percorso sia corretto

        // Icona per Subscriptions
        try (InputStream is = getClass().getResourceAsStream(basePath + iconName)) { // Cambia il nome del file
            if (is != null) {
                Image icon = new Image(is);
                ImageView iconView = new ImageView(icon);
                iconView.setFitHeight(24); // Imposta l'altezza desiderata
                iconView.setFitWidth(24);  // Imposta la larghezza desiderata
                label.setGraphic(iconView);
                label.setGraphicTextGap(5); // Spazio tra icona e testo
            } else {
                System.err.println("Impossibile caricare l'icona: " + basePath + "subscriptions_icon.png");
            }
        } catch (Exception e) {
            System.err.println("Errore nel caricamento dell'icona per Subscriptions: " + e.getMessage());
        }
    }

    private void setAllHomeSections() {
        populateMyNetList();
        populateSubNetList();
        populateDiscoverList();
    }

    private void setUserHome() {
        populateSubNetList();
        populateDiscoverList();
    }

    private void populateMyNetList() {
        if (myNetsSection != null) {
            myNetsSection.setVisible(true);
        }
        if (myNetsList != null) {
            myNetsList.getChildren().clear();
            myNetsList.getChildren().add(createNewNetCard());
        }
    }

    private void populateSubNetList() {
        if (user != null /* && user.hasSubs() */) {
            if (subNetsSection != null) subNetsSection.setVisible(true);
            if (subNetsList != null) {
                subNetsList.getChildren().clear();
                subNetsList.getChildren().add(new PetriNetCard("Subscribed Net A", "Shared project from team.", ""));
                subNetsList.getChildren().add(new PetriNetCard("Shared Model B", "Collaboration work.", ""));
                subNetsList.getChildren().add(new PetriNetCard("Team Process", "Follow updates here.", ""));
                subNetsList.getChildren().add(new PetriNetCard("External Net", "From a community member.", ""));
                subNetsList.getChildren().add(new PetriNetCard("Subscribed Net C", "A very long description that might wrap.", ""));
            }
        } else {
            if (subNetsSection != null) subNetsSection.setVisible(false);
        }
    }

    private void populateDiscoverList() {
        if (user != null /* && user.hasDiscovery() */) {
            if (discoverSection != null) discoverSection.setVisible(true);
            if (discoverList != null) {
                discoverList.getChildren().clear();
                discoverList.getChildren().add(new PetriNetCard("Discover Net 1", "Publicly available net.", ""));
                discoverList.getChildren().add(new PetriNetCard("Popular Model", "Check out this trending net.", ""));
                discoverList.getChildren().add(new PetriNetCard("New Discovery", "Just uploaded, give it a try.", ""));
                discoverList.getChildren().add(new PetriNetCard("Learn Petri", "Educational example.", ""));
            }
        } else {
            if (discoverSection != null) discoverSection.setVisible(false);
        }
    }

    private VBox createNewNetCard() {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(150, 150);
        card.getStyleClass().add("new-net-card");

        Label plusSymbolLabel = new Label("+");
        plusSymbolLabel.getStyleClass().add("plus-symbol");

        Label newNetLabel = new Label("New net");
        newNetLabel.getStyleClass().add("title-label");

        Label descriptionLabel = new Label("Create new Petri net");
        descriptionLabel.getStyleClass().add("description-label");

        card.getChildren().addAll(plusSymbolLabel, newNetLabel, descriptionLabel);

        card.setOnMouseClicked(event -> {
            System.out.println("New Net Card Clicked!");
            // TODO: Implement navigation to the Petri net creation view
        });

        return card;
    }
}