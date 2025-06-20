package it.petrinet.controller;

import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.NavBar;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import it.petrinet.model.User;

import java.awt.event.ActionEvent;
import java.net.URL;

//Easy controller che fa il girno di saluti iniziali

public class MainController {
  @FXML
  private BorderPane mainContainer;
  @FXML private BorderPane navBarContainer;

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



    public void setNavBar(NavBar navBar) {
        navBarContainer.getChildren().clear();
        if(navBar != null) {
            navBarContainer.setLeft(navBar);
            navBarContainer.setMaxWidth(250);
        }

        mainContainer.setLeft(navBarContainer);
    }
  // public void updateNavBar(boolean isAuthenticated) { se mai volessimo mettere
  // la navBar è già fatto
  // navBar.updateAuthStatus(isAuthenticated);
  // }
}
