package it.petrinet.controller;

import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.NavBar;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

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
        ViewNavigator.toLogin();

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
