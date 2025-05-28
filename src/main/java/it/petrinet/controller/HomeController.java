package it.petrinet.controller;

import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
// import it.petrinet.model.DB; // Only import if you actually use it here
import it.petrinet.view.components.PetriNetCard; // Import your PetriNetCard component
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.geometry.Pos; // For Pos.CENTER
import javafx.scene.image.ImageView; // Needed for the plus icon if you re-enable it
import javafx.scene.image.Image;     // Needed for the plus icon if you re-enable it
import javafx.geometry.Insets;      // For Insets
import java.io.InputStream;         // Needed for getClass().getResourceAsStream() if you re-enable images

public class HomeController {

    private User user;

    @FXML
    private VBox homeContainer;

    @FXML
    private Label userNameLabel;

    // IMPORTANT: REMOVED 'static' KEYWORD. These fields will now be injected by FXML.
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
        // Initially hide all sections
        // These will be injected from FXML and should not be null now.
        if (subNetsSection != null) subNetsSection.setVisible(false);
        if (myNetsSection != null) myNetsSection.setVisible(false);
        if (discoverSection != null) discoverSection.setVisible(false);

        // Clear lists in case of re-initialization (good practice)
        // These HBoxes will also be injected and should not be null.
        if (myNetsList != null) myNetsList.getChildren().clear();
        if (subNetsList != null) subNetsList.getChildren().clear();
        if (discoverList != null) discoverList.getChildren().clear();


        // Determine user type and populate sections
        // IMPORTANT: Replace this dummy logic with your actual user role/permission checks
        if (user != null /* && user.isAdmin() */) { // Example condition
            setAllHomeSections(); // Show all sections for demo
        } else {
            // For guest users, maybe only show discover
            setUserHome();
        }
    }

    // Helper to populate all sections (for admin or full access users)
    private void setAllHomeSections() {
        populateMyNetList();
        populateSubNetList();
        populateDiscoverList();
    }

    // Helper for regular users (e.g., only subscriptions and discover)
    private void setUserHome() {
        populateSubNetList();
        populateDiscoverList();
    }

    private void populateMyNetList() {
        // Ensure the section VBox is not null before setting visibility
        if (myNetsSection != null) {
            myNetsSection.setVisible(true);
        }
        // Ensure the HBox list is not null before clearing/adding children
        if (myNetsList != null) {
            myNetsList.getChildren().clear();

            // Add the "New net" card first
            myNetsList.getChildren().add(createNewNetCard());

            // TODO: Replace with actual data loading from your DB or model
            // Example: List<PetriNet> userNets = DB.getUserCreatedNets(user);
            // for (PetriNet net : userNets) {
            //     myNetsList.getChildren().add(new PetriNetCard(net.getTitle(), net.getDescription(), net.getImagePath()));
            // }
        }
    }

    private void populateSubNetList() {
        // IMPORTANT: Use user.hasSubs() check here
        // For demo, assuming user has subs if not null
        if (user != null /* && user.hasSubs() */) {
            if (subNetsSection != null) subNetsSection.setVisible(true);
            if (subNetsList != null) {
                subNetsList.getChildren().clear();

                // TODO: Replace with actual data loading from your DB or model
                // --- Dummy Data for Demonstration (no images) ---
                subNetsList.getChildren().add(new PetriNetCard("Subscribed Net A", "Shared project from team.", ""));
                subNetsList.getChildren().add(new PetriNetCard("Shared Model B", "Collaboration work.", ""));
                subNetsList.getChildren().add(new PetriNetCard("Team Process", "Follow updates here.", ""));
                subNetsList.getChildren().add(new PetriNetCard("External Net", "From a community member.", ""));
                subNetsList.getChildren().add(new PetriNetCard("Subscribed Net C", "A very long description that might wrap.", ""));
                // --- End Dummy Data ---
            }
        } else {
            if (subNetsSection != null) subNetsSection.setVisible(false);
        }
    }

    private void populateDiscoverList() {
        // IMPORTANT: Use user.hasDiscovery() check here
        // For demo, assuming user has discovery if not null
        if (user != null /* && user.hasDiscovery() */) {
            if (discoverSection != null) discoverSection.setVisible(true);
            if (discoverList != null) {
                discoverList.getChildren().clear();

                // TODO: Replace with actual data loading from your DB or model
                // --- Dummy Data for Demonstration (no images) ---
                discoverList.getChildren().add(new PetriNetCard("Discover Net 1", "Publicly available net.", ""));
                discoverList.getChildren().add(new PetriNetCard("Popular Model", "Check out this trending net.", ""));
                discoverList.getChildren().add(new PetriNetCard("New Discovery", "Just uploaded, give it a try.", ""));
                discoverList.getChildren().add(new PetriNetCard("Learn Petri", "Educational example.", ""));
                // --- End Dummy Data ---
            }
        } else {
            if (discoverSection != null) discoverSection.setVisible(false);
        }
    }

    // Helper method to create the "New Net" card (no image)
    private VBox createNewNetCard() {
        VBox card = new VBox(5);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(150, 150);
        card.setStyle("-fx-background-color: #36393f; -fx-background-radius: 8; -fx-border-color: #555; -fx-border-radius: 8; -fx-border-width: 1;"); //Card in style.css
        card.setOnMouseEntered(e -> card.setStyle(card.getStyle() + "-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;"));
        card.setOnMouseExited(e -> card.setStyle(card.getStyle().replace("-fx-cursor: hand; -fx-scale-x: 1.02; -fx-scale-y: 1.02;", "")));

        // Using a Label for the plus symbol instead of an ImageView
        Label plusSymbolLabel = new Label("+");
        plusSymbolLabel.setStyle("-fx-text-fill: #ccc; -fx-font-size: 60px; -fx-font-weight: bold;");

        Label newNetLabel = new Label("New net");
        newNetLabel.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");

        Label descriptionLabel = new Label("Create new Petri net");
        descriptionLabel.setStyle("-fx-text-fill: #aaa; -fx-font-size: 10px;");

        card.getChildren().addAll(plusSymbolLabel, newNetLabel, descriptionLabel);

        card.setOnMouseClicked(event -> {
            System.out.println("New Net Card Clicked!");
            // TODO: Implement navigation to the Petri net creation view
            // Example: ViewNavigator.navigateTo("createNetView.fxml");
        });

        return card;
    }
}