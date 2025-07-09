package it.petrinet.controller;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.model.PetriNet;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.EnhancedAlert;
import it.petrinet.view.components.toolbar.ViewToolBar;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

import it.petrinet.model.Notification;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.NotificationsDAO;

public class NetVisualController implements Initializable {

    public enum VisualState { STARTED, NOT_STARTED, SUBSCRIBABLE }

    private PetriNetViewerPane board;
    private ViewToolBar toolbar;

    @FXML private StackPane boardContainer;
    @FXML private HBox      toolbarContainer;
    @FXML private Button    subscribeButton; // optional, only if SUBSCRIBABLE state

    private static Computation computation;
    private static PetriNet netModel;
    private static VisualState visualState = VisualState.STARTED;

    public static void setVisuals(PetriNet model, Computation data, VisualState state) {
        netModel    = model;
        computation = data;
        visualState = state;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupBoard();

        if (visualState == VisualState.SUBSCRIBABLE) {
            subscribeButton = overlaySubscribeButton();
            boardContainer.getChildren().add(subscribeButton);
        }

        setupToolbar();

        // 1) Allineo la toolbar in alto-centro nello StackPane
        StackPane.setAlignment(toolbarContainer, Pos.TOP_CENTER);
        // 2) Faccio sì che non cresca in verticale oltre la sua altezza pref
        toolbarContainer.setMaxHeight(Region.USE_PREF_SIZE);

        // infine, porto la toolbar in primo piano
        toolbarContainer.toFront();
    }

    private void setupBoard() {
        board = new PetriNetViewerPane(getNetPath(netModel), computation);
        board.prefWidthProperty().bind(boardContainer.widthProperty());
        board.prefHeightProperty().bind(boardContainer.heightProperty());

        board.setOnTransitionFired(this::onTransitionFiredHandler);
        board.setOnPetriNetFinished(this::onPetriNetFinishedHandler);

        boardContainer.getChildren().add(board);
        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(evt -> board.init());
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
    Notification tmp = new Notification(ViewNavigator.getAuthenticatedUser().getUsername(), reciver, netModel.getNetName(), -1,
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
    ComputationStep newComputationStep = new ComputationStep(ComputationsDAO.getIdByComputation(computation), netModel.getNetName(),
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
      String msgTitle = "Activity on %s net!".formatted(netModel.getNetName());
      Notification tmp = null;

      if (t.getType().equals(TRANSITION_TYPE.ADMIN)
          && !username.equals(computation.getCreatorId())) {
        tmp = new Notification(username,
            computation.getCreatorId(), netModel.getNetName(), -1, msgTitle,
            getNotificationText(isFinished, username, transitionName, t.getName()), System.currentTimeMillis() / 1000);

      } else if (t.getType().equals(TRANSITION_TYPE.USER)
          && username.equals(computation.getCreatorId())) {
        tmp = new Notification(
            username, computation.getUserId(), netModel.getNetName(), -1, msgTitle,
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

    private Button overlaySubscribeButton() {
        Button subscribe = new Button("Subscribe");
        subscribe.setMaxWidth(350);
        subscribe.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1E1E2E; -fx-font-size: 16; -fx-background-radius: 8; -fx-padding: 10;");
        subscribe.setOnAction(e -> handleSubscribe());
        subscribe.setPickOnBounds(false);
        StackPane.setAlignment(subscribe, Pos.BOTTOM_CENTER);
        StackPane.setMargin(subscribe, new Insets(10, 10, 40, 10));

        return subscribe;
    }

    private void setupToolbar() {
        toolbar = new ViewToolBar(board, this);
        switch (visualState) {
            case STARTED     -> toolbar.startedButton();
            case NOT_STARTED -> toolbar.startableButton();
            case SUBSCRIBABLE-> toolbar.subButton();
        }
        toolbarContainer.getChildren().add(toolbar);
    }

    private void handleSubscribe() {
        computation = new Computation(
                netModel.getNetName(),
                netModel.getCreatorId(),
                ViewNavigator.getAuthenticatedUser().getUsername()
        );

        ComputationsDAO.insertComputation(computation);

        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subscribeButton);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);

        // When animation completes, hide and disable the button
        fadeOut.setOnFinished(event -> {
            subscribeButton.setDisable(true);
            subscribeButton.setManaged(false);
            subscribeButton.setVisible(false);
        });

        // Start the animation
        fadeOut.play();

        toolbar.startableButton();
    }


    public void restartAction() {
        Optional<EnhancedAlert.AlertResult> result = EnhancedAlert.showConfirmation(
                "Restart Petri Net",
                "Are you sure you want to restart the Petri Net?\n This will reset the current computation and start over."
        );

        if(result.get().isCancel()) return;

        if (result.get().isOK()) {
                ComputationStepDAO.removeAllStepsByComputation(computation);
                ComputationsDAO.setAsStarted(computation, System.currentTimeMillis());
                //TODO: Update computation
                board.setComputation(computation);
                board.updateComputation();
                toolbar.startableButton();
        }

    }

    public void unsubscribeAction() {
        Optional<EnhancedAlert.AlertResult> result = EnhancedAlert.showConfirmation(
                "Unsubscribe from Petri Net",
                "Are you sure you want to unsubscribe from the Petri Net?\n This will remove your subscription and reset your computation."
        );

        if(result.get().isCancel()) return;

        if( result.get().isOK()) {
                ComputationStepDAO.removeAllStepsByComputation(computation);
                //ComputationsDAO.unsubscribeFromNet(computation, ViewNavigator.getAuthenticatedUser().getUsername());
                board.setComputation(null);
                toolbar.subButton();
        }

    }

    public void startAction() {
        ComputationsDAO.getIdByComputation(computation);
        System.out.println(computation);
        ComputationStep step = new ComputationStep(
                68,
                ComputationsDAO.getIdByComputation(computation),
                netModel.getNetName(),
                "",
                board.getStartPlaceName() + ":1",
                System.currentTimeMillis()/1000
        );

        computation.addStep(step);
        board.setComputation(computation);
        ComputationStepDAO.insertStep(step);
        ComputationsDAO.setAsStarted(computation, System.currentTimeMillis()/1000);
    }

    private static String getNetPath(PetriNet netModel) {
        return System.getProperty("user.dir") + "/src/main/resources/data/pnml/" + netModel.getXML_PATH();
    }


}
