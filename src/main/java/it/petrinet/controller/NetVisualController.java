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
import it.petrinet.model.database.ComputationStepDAO;
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
    board.setOnPetriNetFinished(this::onPetriNetFinishedHandler);

    boardContainer.getChildren().add(board);

    PauseTransition delay = new PauseTransition(Duration.millis(200));
    delay.setOnFinished(_ -> {
      board.init();
    });
    delay.play();
  }

  /**
   * This handler is called when the petri net is finished
   */
  void onPetriNetFinishedHandler() {
    ComputationsDAO.setAsCompleted(computation, System.currentTimeMillis() / 1000);
    String reciver = (ViewNavigator.getAuthenticatedUser().getUsername().equals(computation.getCreatorId()))
        ? computation.getUserId()
        : computation.getCreatorId();
    String text = "%s just reached finish place!".formatted(ViewNavigator.getAuthenticatedUser().getUsername());
    Notification tmp = new Notification(ViewNavigator.getAuthenticatedUser().getUsername(), reciver, baseNet, -1,
        "Net finished!", text, System.currentTimeMillis() / 1000);
    NotificationsDAO.insertNotification(tmp);
  }

  /**
   * This handler is called every time a Transition is fired
   *
   * @param transitionName  clicked transition name
   * @param newMarkingState new token status, after transition is fired
   * @param newTransition   new firable transition
   */
  void onTransitionFiredHandler(String transitionName, Map<String, Integer> newMarkingState,
      List<Transition> newTransition) {

    // check if, by firing transitionName, some token reached finish place
    boolean isFinished = newMarkingState.keySet().stream().anyMatch(p -> p.equals(board.getFinishPlaceName()));

    // - Create a new ComputationStep with: clicked transition, new marking state.
    // - Add this step to the DB
    ComputationStep newComputationStep = new ComputationStep(ComputationsDAO.getIdByComputation(computation), baseNet,
        transitionName,
        newMarkingState,
        System.currentTimeMillis() / 1000);
    ComputationStepDAO.insertStep(newComputationStep);

    // - Compute the new nextStep state based on "newTransition" types
    // - Update this value for the current computation in the DB
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
    ComputationsDAO.setNextStepType(computation, nextStep);

    // Iterate over new firable transitions and notify the user which transitions
    // can fire
    newTransition.forEach(t -> {
      String username = ViewNavigator.getAuthenticatedUser().getUsername();
      String msgTitle = "Activity on %s net!".formatted(baseNet);
      Notification tmp = null;

      if (t.getType().equals(TRANSITION_TYPE.ADMIN)
          && !username.equals(computation.getCreatorId())) {
        tmp = new Notification(username,
            computation.getCreatorId(), baseNet, -1, msgTitle,
            getNotificationText(isFinished, username, transitionName, t.getName()), System.currentTimeMillis() / 1000);

      } else if (t.getType().equals(TRANSITION_TYPE.USER)
          && username.equals(computation.getCreatorId())) {
        tmp = new Notification(
            computation.getCreatorId(), username, baseNet, -1, msgTitle,
            getNotificationText(isFinished, username, transitionName, t.getName()), System.currentTimeMillis() / 1000);
      }
      if (tmp != null) {
        NotificationsDAO.insertNotification(tmp);
      }
    });
  }

  private String getNotificationText(boolean isFinished, String sender, String firedTransition,
      String newPossibileTransition) {
    if (!isFinished) {
      if (firedTransition.isEmpty()) {
        return "Now tou can fire '%s'!".formatted(sender, firedTransition, newPossibileTransition);
      }
      return "%s fired %s transition, now you can  fire '%s'!".formatted(sender, firedTransition,
          newPossibileTransition);
    } else {
      return "%s reach finish node by firing %s transition".formatted(sender, firedTransition);
    }
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
