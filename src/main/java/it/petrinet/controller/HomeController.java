package it.petrinet.controller;

import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import it.petrinet.model.DB;
import javafx.scene.control.Label;
import javafx.fxml.FXML;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class HomeController {

    private User user;

    @FXML
    private VBox homeContainer;

    @FXML
    private Label myNets;
    @FXML
    private Label subNets;
    @FXML
    private Label discorver;

    @FXML
    private HBox myNetsList;
    @FXML
    private HBox subNetsList;
    @FXML
    private HBox discoverList;

    private void initialize() {
        this.user = ViewNavigator.getAuthenticatedUser();

        // Clear current contents and repopulate
        homeContainer.getChildren().clear();
        setAdminHome(); // or setUserHome(), depending on your logic

        // Add the components (labels and lists)
        homeContainer.getChildren().addAll(
                myNets, myNetsList,
                subNets, subNetsList,
                discorver, discoverList
        );
    }

    private void setAdminHome() {
        populateMyNetList();
        populateSubNetList();
        populateDiscoverList();
    }

    private void setUserHome() {
        populateSubNetList();
        populateDiscoverList();
    }

    private void populateMyNetList() {
        if(user.hasCreation()){
           myNets.setVisible(true);
           myNetsList.setVisible(true);

           myNetsList.getChildren().clear();


        }
    }

    private void populateSubNetList() {
        if(user.hasSubs()){
            subNets.setVisible(true);
            subNetsList.setVisible(true);
        }
    }

    private void populateDiscoverList() {
        if(user.hasDiscovery()){
            discorver.setVisible(true);
            discoverList.setVisible(true);
        }
    }


}
