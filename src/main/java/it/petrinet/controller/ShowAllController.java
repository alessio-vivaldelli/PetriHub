// src/main/java/it/petrinet/view/ShowAllController.java
package it.petrinet.controller;

import it.petrinet.model.DB;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.NetCategory;
import it.petrinet.view.components.PetriNetCard;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

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
        for( String card : DB.getNets() ) {
            cardContainer.getChildren().add(new PetriNetCard(card, "blabla", "dummy.png"));
        }
    }

    public void onBack(){
        ViewNavigator.navigateToHome();
    }

}
