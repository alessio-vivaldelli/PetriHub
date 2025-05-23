package it.petrinet.controller;

import it.petrinet.view.ViewNavigator;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import it.petrinet.model.User;

import java.awt.event.ActionEvent;

//Easy controller che fa il girno di saluti iniziali

public class MainController {
    @FXML
    private BorderPane mainContainer;

    @FXML
    public void initialize() {

        // Register this controller with the ViewNavigator
        ViewNavigator.init(this);

        // Load the home view by default
        ViewNavigator.navigateToLogin();
    }

    /**
     * Set the content of the main area
     */
    public void setContent(Node content) {
        mainContainer.setCenter(content);
    }


   // public void updateNavBar(boolean isAuthenticated) {  se mai volessimo mettere la navBar è già fatto
   //     navBar.updateAuthStatus(isAuthenticated);
   // }
}