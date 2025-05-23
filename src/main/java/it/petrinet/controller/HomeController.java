package it.petrinet.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import it.petrinet.Main;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.NavBar;
import javafx.scene.layout.HBox;


public class HomeController {

    @FXML
    private HBox navBarContainer;

    private NavBar navBar;


    private void initialize() {
        navBar = new NavBar();
    }

}
