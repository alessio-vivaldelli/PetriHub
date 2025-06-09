// src/main/java/it/petrinet/view/ShowAllController.java
package it.petrinet.controller;

import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.model.NetCategory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;

public class ShowAllController {

    @FXML private ScrollPane scrollPane;
    @FXML private Label frameTitle;
    @FXML private FlowPane cardContainer;
    private static NetCategory cardType;
    @FXML private Button backButton;

    public static void setType(NetCategory type) {
        cardType = type;
    }

    @FXML
    public void initialize() {
        frameTitle.setText(cardType.getDisplayName());
        IconUtils.setIcon(frameTitle, cardType.getDisplayName() + ".png", 30, 30 , Color.WHITE);
        IconUtils.setIcon(backButton, "backArrow.png", 30, 30, Color.valueOf("#181825"));
        backButton.setStyle("-fx-background-radius: 50%; -fx-min-width: 40px; -fx-min-height: 40px; -fx-max-width: 40px; -fx-max-height: 40px; ");
        cardContainer.setVgap(20);
        cardContainer.setHgap(20);
        cardContainer.setPrefWrapLength(400); // Set preferred width for wrapping
        loadCards();
    }

    private void loadCards() {

    }

    public void onBack(){
        ViewNavigator.navigateToHome();
    }

}
