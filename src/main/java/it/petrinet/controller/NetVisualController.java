package it.petrinet.controller;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.Notification;
import it.petrinet.model.PetriNet;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.NotificationsDAO;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.service.SessionContext;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.components.EnhancedAlert;
import it.petrinet.view.components.NotificationFactory;
import it.petrinet.view.components.toolbar.ViewToolBar;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.PauseTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class NetVisualController {

  public enum VisualState {
    STARTED, NOT_STARTED, SUBSCRIBABLE
  }

  // --- FXML Injected Fields ---
  @FXML
  private StackPane boardContainer;
  @FXML
  private HBox toolbarContainer;

  // --- UI Components ---
  private PetriNetViewerPane board;
  private ViewToolBar toolbar;
  private Button subscribeButton;
  private VBox historyPane;

  private Computation computation;
  private PetriNet netModel;
  private VisualState visualState;

  // Constants
  private static final String HISTORY_LIST_VIEW_ID = "historyListView";
  private static final DateTimeFormatter HISTORY_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

  public void initData(PetriNet model, Computation data, VisualState state) {
    this.netModel = model;
    this.computation = data;
    this.visualState = state;

    setupBoard();
    setupToolbar();
    updateUiForState(this.visualState);
  }

  @FXML
  public void initialize() {
    toolbarContainer.setMaxHeight(Region.USE_PREF_SIZE);
    StackPane.setAlignment(toolbarContainer, Pos.TOP_CENTER);
  }

  // =================================================================================
  // PUBLIC ACTIONS
  // =================================================================================

  public PetriNetViewerPane getBoard() {
    return board;
  }

  public void startAction() {
    long timestamp = System.currentTimeMillis() / 1000;
    Map<String, Integer> startMarking = Map.of(board.getStartPlaceName(), 1);

    ComputationStep step = new ComputationStep(
        ComputationsDAO.getIdByComputation(computation),
        netModel.getNetName(),
        "",
        startMarking,
        timestamp);

    computation.addStep(step);
    ComputationStepDAO.insertStep(step);
    ComputationsDAO.setAsStarted(computation, timestamp);

    processNextStepAndNotifications(board.setComputation(computation), NotificationFactory.MessageType.STARTED_COMPUTATION);
    updateUiForState(VisualState.STARTED);

    updateHistoryIfVisible();
  }

  public void restartAction() {
    showConfirmationAlert(
        "Restart Petri Net",
        "Are you sure you want to restart? This will reset the current computation.",
        () -> {
          ComputationStepDAO.removeAllStepsByComputation(computation);
          ComputationsDAO.restartComputation(computation);
          computation.restart();
          processNextStepAndNotifications(NotificationFactory.MessageType.RESTART);
          board.setComputation(computation);
          updateUiForState(VisualState.NOT_STARTED);
          updateHistoryIfVisible();
        });
  }

  public void unsubscribeAction() {
    showConfirmationAlert(
        "Unsubscribe from Petri Net",
        "Are you sure you want to unsubscribe? This will remove your subscription.",
        () -> {
          ComputationStepDAO.removeAllStepsByComputation(computation);
          ComputationsDAO.removeComputation(computation);
          updateUiForState(VisualState.SUBSCRIBABLE);

          processNextStepAndNotifications(NotificationFactory.MessageType.UNSUBSCRIBE);

          board.setComputation(null);
          this.computation = null;
          clearHistoryPane();
        });
  }

  public void toggleHistory() {
    if (historyPane == null) {
      createHistoryPane();
    }

    if (historyPane.isVisible()) {
      animateHistoryPane(false);
    } else {
      updateAndShowHistory();
    }
  }

  // =================================================================================
  // EVENT HANDLERS
  // =================================================================================


  private void onPetriNetFinishedHandler() {
    long timestamp = System.currentTimeMillis() / 1000;
    ComputationsDAO.setAsCompleted(computation, timestamp);

    processNextStepAndNotifications(NotificationFactory.MessageType.END_COMPUTATION);

    EnhancedAlert.showInformation(
            "Petri Net Finished",
            "The Petri Net computation has been completed successfully."
    );
  }

  // =================================================================================
  // UI SETUP
  // =================================================================================

  private void setupBoard() {
    String netPath = System.getProperty("user.dir") + "/src/main/resources/data/pnml/" + netModel.getXML_PATH();
    board = new PetriNetViewerPane(netPath, computation);
    board.prefWidthProperty().bind(boardContainer.widthProperty());
    board.prefHeightProperty().bind(boardContainer.heightProperty());

    board.setOnTransitionFired(this::onTransitionFiredHandler);
    board.setOnPetriNetFinished(this::onPetriNetFinishedHandler);

    boardContainer.getChildren().add(board);

    PauseTransition delay = new PauseTransition(Duration.millis(200));
    delay.setOnFinished(evt -> board.init());
    delay.play();
  }

  private void setupToolbar() {
    toolbar = new ViewToolBar(this);
    toolbarContainer.getChildren().add(toolbar);
  }

  private void updateUiForState(VisualState newState) {
    this.visualState = newState;

    if (newState == VisualState.SUBSCRIBABLE) {
      if (subscribeButton == null) {
        subscribeButton = createSubscribeButton();
        boardContainer.getChildren().add(subscribeButton);
      }
      subscribeButton.setManaged(true);
      subscribeButton.setVisible(true);
      subscribeButton.setOpacity(1);
      subscribeButton.setDisable(false);
    } else if (subscribeButton != null) {
      subscribeButton.setManaged(false);
      subscribeButton.setVisible(false);
    }

    switch (newState) {
      case STARTED -> toolbar.configureForStarted();
      case NOT_STARTED -> toolbar.configureForStartable();
      case SUBSCRIBABLE -> toolbar.configureForSubscribable();
    }
    toolbarContainer.toFront();

  }

  private Button createSubscribeButton() {
    Button button = new Button("Subscribe");
    button.setMaxWidth(350);
    button.getStyleClass().add("subscribe-button");
    button.setOnAction(e -> handleSubscribe());
    button.setPickOnBounds(false);
    StackPane.setAlignment(button, Pos.BOTTOM_CENTER);
    StackPane.setMargin(button, new Insets(10, 10, 40, 10));
    return button;
  }

  private void handleSubscribe() {
    computation = new Computation(
        netModel.getNetName(),
        netModel.getCreatorId(),
        SessionContext.getInstance().getUser().getUsername()
        );
    ComputationsDAO.insertComputation(computation);
    processNextStepAndNotifications(board.setComputation(computation), NotificationFactory.MessageType.SUBSCRIPTION);

    FadeTransition fadeOut = new FadeTransition(Duration.millis(200), subscribeButton);
    fadeOut.setFromValue(1.0);
    fadeOut.setToValue(0.0);
    fadeOut.setOnFinished(event -> updateUiForState(VisualState.NOT_STARTED));
    fadeOut.play();
  }

  // =================================================================================
  // HISTORY PANE
  // =================================================================================

  private void createHistoryPane() {
    historyPane = new VBox(10);
    historyPane.getStyleClass().add("history-pane");

    // Header
    HBox header = new HBox(10);
    header.getStyleClass().add("history-header");

    Label historyIcon = new Label();
    IconUtils.setIcon(historyIcon, "history.png", 35);

    Label titleLabel = new Label("Computation History");
    titleLabel.getStyleClass().add("history-title");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    Button closeButton = new Button("✕");
    closeButton.getStyleClass().add("history-close-button");
    closeButton.setOnAction(e -> animateHistoryPane(false));

    header.getChildren().addAll(historyIcon, titleLabel, spacer, closeButton);

    // List View - with transparent background
    ListView<ComputationStep> historyListView = new ListView<>();
    historyListView.setId(HISTORY_LIST_VIEW_ID);
    historyListView.getStyleClass().add("history-list-view");
    historyListView.setBackground(Background.EMPTY); // Crucial Java fix
    VBox.setVgrow(historyListView, Priority.ALWAYS);

    // Placeholder
    Label placeholderLabel = new Label("No computation steps recorded yet");
    placeholderLabel.getStyleClass().add("history-placeholder");
    historyListView.setPlaceholder(placeholderLabel);

    historyPane.getChildren().addAll(header, historyListView);
    historyPane.setMaxSize(420, 500);
    historyPane.setVisible(false);
    historyPane.setManaged(false);

    boardContainer.getChildren().add(historyPane);
    StackPane.setAlignment(historyPane, Pos.TOP_RIGHT);
    StackPane.setMargin(historyPane, new Insets(20));
  }

  private void updateAndShowHistory() {
    ListView<ComputationStep> historyListView = (ListView<ComputationStep>) historyPane
        .lookup("#" + HISTORY_LIST_VIEW_ID);

    historyListView.getItems().clear();
    if (computation != null) {
      computation.getSteps().stream()
          .sorted(Comparator.reverseOrder())
          .forEach(historyListView.getItems()::add);
    }

    historyListView.setCellFactory(lv -> new ListCell<>() {
      private final HBox container = new HBox(12);
      private final Circle indicator = new Circle(5);
      private final Label transitionLabel = new Label();
      private final Label timestampLabel = new Label();
      private final Region spacer = new Region();

      {
        // One-time setup
        container.setAlignment(Pos.CENTER_LEFT);
        container.getChildren().addAll(indicator, transitionLabel, spacer, timestampLabel);
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Apply CSS classes
        container.getStyleClass().add("history-item-container");
        transitionLabel.getStyleClass().add("history-transition-label");
        timestampLabel.getStyleClass().add("history-timestamp-label");

        // Set transparent background
        setBackground(Background.EMPTY);
        setStyle("-fx-background-color: transparent;");

        setGraphic(container);
        setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      }

      @Override
      protected void updateItem(ComputationStep step, boolean empty) {
        super.updateItem(step, empty);
        if (empty || step == null) {
          setGraphic(null);
        } else {
          LocalDateTime dateTime = LocalDateTime.ofInstant(
              Instant.ofEpochSecond(step.getTimestamp()),
              ZoneId.systemDefault());

          boolean isStartStep = step.getTransitionName() == null ||
              step.getTransitionName().isEmpty();

          boolean isTransitionAdmin = board.getTypeByTransitionName(step.getTransitionName())
              .equals(TRANSITION_TYPE.ADMIN);

          // Update indicator color
          // se il nome della transizione è vuota
          if (isStartStep) {
            indicator.setFill(Color.web("#89b4fa"));
            container.getStyleClass().removeAll("history-user-transition", "history-admin-transition");
          } else if (isTransitionAdmin) { // se è l'ultima nella tabella (dal basso verso l'alto)
            indicator.setFill(Color.web("#f38ba8"));
            container.getStyleClass().add("history-admin-transition");
            container.getStyleClass().remove("history-user-transition");
          } else {
            indicator.setFill(Color.web("#a6e3a1"));
            container.getStyleClass().add("history-user-transition");
            container.getStyleClass().remove("history-admin-transition");
          }

          // Update labels
          transitionLabel.setText(isStartStep ? "Initial State" : step.getTransitionName());
          timestampLabel.setText(dateTime.format(HISTORY_DATE_FORMATTER));

          setGraphic(container);
        }
      }
    });

    animateHistoryPane(true);
  }

  private void animateHistoryPane(boolean show) {
    TranslateTransition tt = new TranslateTransition(Duration.millis(300), historyPane);
    FadeTransition ft = new FadeTransition(Duration.millis(300), historyPane);

    if (show) {
      historyPane.setTranslateX(20);
      historyPane.setManaged(true);
      historyPane.setVisible(true);

      tt.setFromX(20);
      tt.setToX(0);
      tt.setInterpolator(Interpolator.EASE_OUT);

      ft.setFromValue(0.0);
      ft.setToValue(1.0);
    } else {
      tt.setFromX(0);
      tt.setToX(20);
      tt.setInterpolator(Interpolator.EASE_IN);

      ft.setFromValue(1.0);
      ft.setToValue(0.0);
      ft.setOnFinished(e -> {
        historyPane.setVisible(false);
        historyPane.setManaged(false);
      });
    }

    ParallelTransition pt = new ParallelTransition(tt, ft);
    pt.play();
  }

  private void updateHistoryIfVisible() {
    if (historyPane != null && historyPane.isVisible()) {
      updateAndShowHistory();
    }
  }

  private void clearHistoryPane() {
    if (historyPane != null) {
      ListView<ComputationStep> historyListView = (ListView<ComputationStep>) historyPane
          .lookup("#" + HISTORY_LIST_VIEW_ID);
      historyListView.getItems().clear();
      animateHistoryPane(false);
    }
  }

  // =================================================================================
  // NOTIFICATION HELPERS
  // =================================================================================

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
    ComputationStep newComputationStep = new ComputationStep(ComputationsDAO.getIdByComputation(computation),
            netModel.getNetName(),
            transitionName,
            newMarkingState,
            System.currentTimeMillis() / 1000);

    ComputationStepDAO.insertStep(newComputationStep);

    computation.addStep(newComputationStep);

    processNextStepAndNotifications(newTransition, NotificationFactory.MessageType.FIRED_TRANSITION);
    updateHistoryIfVisible();
  }

  private void processNextStepAndNotifications( NotificationFactory.MessageType type) {
    processNextStepAndNotifications(new ArrayList<>(), type);
  }

  private void processNextStepAndNotifications(List<Transition> transitions, NotificationFactory.MessageType type) {
    int nextStep = computeNextStepType(transitions);
    ComputationsDAO.setNextStepType(computation, nextStep);

    String username = SessionContext.getInstance().getUser().getUsername();
    String receiver = (username.equals(computation.getCreatorId())) ? computation.getUserId() : computation.getCreatorId();

    Notification caseNotification = null;
    caseNotification = new Notification(username, receiver, netModel.getNetName(), type.ordinal(),
                System.currentTimeMillis() / 1000);
    NotificationsDAO.insertNotification(caseNotification);

   transitions.forEach(t -> {
      Notification tmp = null;
      if (t.getType().equals(TRANSITION_TYPE.ADMIN) && !username.equals(computation.getCreatorId())) {
        tmp = new Notification(username, computation.getCreatorId(), netModel.getNetName(), NotificationFactory.MessageType.AVAILABLE_TRANSITION.ordinal(),
                System.currentTimeMillis() / 1000);
      } else if (t.getType().equals(TRANSITION_TYPE.USER) && username.equals(computation.getCreatorId())) {
        tmp = new Notification(username, computation.getUserId(), netModel.getNetName(), NotificationFactory.MessageType.AVAILABLE_TRANSITION.ordinal(),
                System.currentTimeMillis() / 1000);
      }
      if (tmp != null) {
        NotificationsDAO.insertNotification(tmp);
      }
    });

  }

  private int computeNextStepType(List<Transition> transitions) {
    boolean isNextUser = transitions.stream().anyMatch(t -> t.getType().equals(TRANSITION_TYPE.USER));
    boolean isNextAdmin = transitions.stream().anyMatch(t -> t.getType().equals(TRANSITION_TYPE.ADMIN));

    if (isNextUser && isNextAdmin)
      return 3;
    if (isNextAdmin)
      return 2;
    if (isNextUser)
      return 1;
    return 0;
  }

  // =================================================================================
  // UI HELPERS
  // =================================================================================

  private void showConfirmationAlert(String title, String content, Runnable onConfirm) {
    EnhancedAlert.showConfirmation(title, content)
        .filter(EnhancedAlert.AlertResult::isYes)
        .ifPresent(result -> onConfirm.run());
  }

  /* Utils */
  public boolean isCreator() {
    return netModel.getCreatorId().equals(SessionContext.getInstance().getUser().getUsername());
  }

}
