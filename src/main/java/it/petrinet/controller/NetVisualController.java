package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.toolbar.ViewToolBar;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import it.petrinet.model.Notification;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.NotificationsDAO;

public class NetVisualController implements Initializable {

  private PetriNetViewerPane board;

  @FXML
  private VBox boardContainer = new VBox();
  @FXML
  private HBox toolbarContainer;

  private static Computation computation;
  private static String baseNet;

  public static void setVisuals(String path, Computation data) {
    baseNet = path;
    computation = data;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    System.out.println(computation);
    setupBoard();
    setupToolbar();
  }

  private void setupBoard() {
    boardContainer.setSpacing(10);

    board = new PetriNetViewerPane(baseNet, computation);
    board.prefWidthProperty().bind(boardContainer.widthProperty());
    board.prefHeightProperty().bind(boardContainer.heightProperty());

    board.setOnPetriNetFinished(() -> {
      System.out.println("Petri net finished.");
    });

    board.setOnTransitionFired(this::onTransitionFiredHandler);

    board.setOnPetriNetFinished(null); // TODO: set method for this hook

    boardContainer.getChildren().add(board);

    PauseTransition delay = new PauseTransition(Duration.millis(200));
    delay.setOnFinished(_ -> {
      board.init();
    });
    delay.play();
  }

  void onTransitionFiredHandler(String transitionName, Map<String, Integer> newMarkingState,
      List<Transition> newTransition) {
    ComputationStep newComputationStep = null;
    try {
      newComputationStep = new ComputationStep(ComputationsDAO.getIdByComputation(computation), baseNet, transitionName,
          newMarkingState,
          System.currentTimeMillis() / 1000);
    } catch (InputTypeException e) {
      e.printStackTrace();
    }

    int nextStep = 0;
    boolean isNextUser = newTransition.stream().anyMatch(t -> t.getType().equals(TRANSITION_TYPE.USER));
    boolean isNextAdmin = newTransition.stream().anyMatch(t -> t.getType().equals(TRANSITION_TYPE.ADMIN));
    if (isNextUser && isNextAdmin) {
      nextStep = 3;
    } else if (isNextAdmin) {
      nextStep = 2;
    } else if (isNextUser) {
      nextStep = 1;
    }

    newTransition.forEach(t -> {
      String username = ViewNavigator.getAuthenticatedUser().getUsername();
      String msgTitle = "Waiting for action";
      Notification tmp = null;

      if (t.getType().equals(TRANSITION_TYPE.ADMIN)
          && !username.equals(computation.getCreatorId())) {
        tmp = new Notification(username,
            computation.getCreatorId(), baseNet, -1, msgTitle,
            getNotificationText(username, transitionName, t.getName()), System.currentTimeMillis() / 1000);

      } else if (t.getType().equals(TRANSITION_TYPE.USER)
          && username.equals(computation.getCreatorId())) {
        tmp = new Notification(
            computation.getCreatorId(), username, baseNet, -1, msgTitle,
            getNotificationText(username, transitionName, t.getName()), System.currentTimeMillis() / 1000);
      }
      if (tmp != null) {
        try {
          NotificationsDAO.insertNotification(tmp);
        } catch (InputTypeException e) {
          e.printStackTrace();
        }
      }

    });

    // TODO: use updateNextStep on computation to update nextStpe field on
    // computation

  }

  private String getNotificationText(String sender, String firedTransition, String newPossibileTransition) {
    return "%s fired %s transition, now tou can  fire %s!".formatted(sender, firedTransition, newPossibileTransition);
  }

  // TODO: Implement the logic to retrieve the Computation object from the
  // database
  private Computation getComputationFromDB() {
    // TODO: Replace with actual database retrieval logic
    Computation computation = new Computation("testnet", "creatorID", "userID");
    computation.addStep(new ComputationStep(1, 1, "testnet", "", "start_e:1", 123456));

    return computation;
  }

  private void setupToolbar() {
    ViewToolBar toolbar = new ViewToolBar(board);
    toolbarContainer.getChildren().add(toolbar);
  }

  // Additional methods for handling user interactions, updating the view, etc.
  // can be added here
}
