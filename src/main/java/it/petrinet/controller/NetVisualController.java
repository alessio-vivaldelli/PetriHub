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
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.EnhancedAlert;
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

    public enum VisualState {STARTED, NOT_STARTED, SUBSCRIBABLE}

    // --- FXML Injected Fields ---
    @FXML private StackPane boardContainer;
    @FXML private HBox toolbarContainer;

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
    private static final DateTimeFormatter HISTORY_DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

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

    public void startAction() {
        long timestamp = System.currentTimeMillis() / 1000;
        Map<String, Integer> startMarking = Map.of(board.getStartPlaceName(), 1);

        ComputationStep step = new ComputationStep(
                ComputationsDAO.getIdByComputation(computation),
                netModel.getNetName(),
                "",
                startMarking,
                timestamp
        );

        computation.addStep(step);
        ComputationStepDAO.insertStep(step);
        ComputationsDAO.setAsStarted(computation, timestamp);

        List<Transition> firableTransitions = board.setComputation(computation);
        sendNotificationFirableTransitions(firableTransitions, true);
        updateUiForState(VisualState.STARTED);

        updateHistoryIfVisible();
    }

    public void restartAction() {
        showConfirmationAlert(
                "Restart Petri Net",
                "Are you sure you want to restart? This will reset the current computation.",
                () -> {
                    ComputationStepDAO.removeAllStepsByComputation(computation);
                    ComputationsDAO.setAsStarted(computation, 0);
                    ComputationsDAO.setAsCompleted(computation, 0);
                    computation.clearSteps();
                    computation.setEndDate(0);
                    computation.setStartDate(0);

                    List<Transition> firableTransitions = board.setComputation(computation);
                    sendNotificationFirableTransitions(firableTransitions, true);
                    updateUiForState(VisualState.NOT_STARTED);

                    updateHistoryIfVisible();
                }
        );
    }

    public void unsubscribeAction() {
        showConfirmationAlert(
                "Unsubscribe from Petri Net",
                "Are you sure you want to unsubscribe? This will remove your subscription.",
                () -> {
                    ComputationStepDAO.removeAllStepsByComputation(computation);
                    ComputationsDAO.removeComputation(computation);
                    board.setComputation(null);
                    this.computation = null;
                    updateUiForState(VisualState.SUBSCRIBABLE);

                    clearHistoryPane();
                }
        );
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

    private void onTransitionFiredHandler(String transitionName, Map<String, Integer> newMarkingState,
                                          List<Transition> newFirableTransitions) {
        boolean isFinished = newMarkingState.containsKey(board.getFinishPlaceName());
        long timestamp = System.currentTimeMillis() / 1000;

        ComputationStep newStep = new ComputationStep(
                ComputationsDAO.getIdByComputation(computation),
                netModel.getNetName(),
                transitionName,
                newMarkingState,
                timestamp
        );

        ComputationStepDAO.insertStep(newStep);
        computation.addStep(newStep);

        processNextStepAndNotifications(newFirableTransitions, transitionName, isFinished);
        updateHistoryIfVisible();
    }

    private void onPetriNetFinishedHandler() {
        long timestamp = System.currentTimeMillis() / 1000;
        ComputationsDAO.setAsCompleted(computation, timestamp);

        String sender = ViewNavigator.getAuthenticatedUser().getUsername();
        String receiver = computation.getCreatorId().equals(sender) ?
                computation.getUserId() : computation.getCreatorId();

        Notification notification = new Notification(
                sender,
                receiver,
                netModel.getNetName(),
                -1,
                "Net finished!",
                sender + " just reached the finish place!",
                timestamp
        );
        NotificationsDAO.insertNotification(notification);
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
        toolbar = new ViewToolBar(board, this);
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
                ViewNavigator.getAuthenticatedUser().getUsername()
        );
        ComputationsDAO.insertComputation(computation);

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
        ListView<ComputationStep> historyListView =
                (ListView<ComputationStep>) historyPane.lookup("#" + HISTORY_LIST_VIEW_ID);

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
                            ZoneId.systemDefault()
                    );

                    boolean isStartStep = step.getTransitionName() == null ||
                            step.getTransitionName().isEmpty();
                    boolean isLatest = getIndex() == 0;

                    String colorByTransitionType = (board.getTypeByTransitionName(step.getTransitionName()).equals(TRANSITION_TYPE.ADMIN)) ? "#f38ba8" : "#89b4fa";

                    // Update indicator color
                    // se il nome della transizione è vuota
                    if (isStartStep) {
                        indicator.setFill(Color.web("#a6e3a1"));
                        container.getStyleClass().add("history-start-step");
                        container.getStyleClass().remove("history-latest-transition");
                    } else if (isLatest) { // se è l'ultima nella tabella (dal basso verso l'alto)
                        indicator.setFill(Color.web("#f38ba8"));
                        container.getStyleClass().add("history-latest-transition");
                        container.getStyleClass().remove("history-start-step");
                    } else { // se non è l'ulima e non è la prima
                        indicator.setFill(Color.web("#89b4fa"));
                        container.getStyleClass().removeAll("history-start-step", "history-latest-transition");
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
            ListView<ComputationStep> historyListView =
                    (ListView<ComputationStep>) historyPane.lookup("#" + HISTORY_LIST_VIEW_ID);
            historyListView.getItems().clear();
            animateHistoryPane(false);
        }
    }

    // =================================================================================
    // NOTIFICATION HELPERS
    // =================================================================================

    private void processNextStepAndNotifications(List<Transition> firableTransitions,
                                                 String firedTransition,
                                                 boolean isFinished) {
        int nextStepType = computeNextStepType(firableTransitions);
        ComputationsDAO.setNextStepType(computation, nextStepType);

        String sender = ViewNavigator.getAuthenticatedUser().getUsername();
        String title = "Activity on " + netModel.getNetName() + " net!";

        for (Transition t : firableTransitions) {
            String text = getNotificationText(isFinished, sender, firedTransition, t.getName());
            createAndSendNotification(sender, t.getType(), title, text);
        }
    }

    private void sendNotificationFirableTransitions(List<Transition> transitions, boolean isInitialization) {
        String username = ViewNavigator.getAuthenticatedUser().getUsername();
        String msgTitle = "Activity on " + netModel.getNetName() + " net!";

        for (Transition t : transitions) {
            String text = isInitialization
                    ? username + " just started the net, now you can fire '" + t.getName() + "'!"
                    : getNotificationText(false, username, "", t.getName());

            createAndSendNotification(username, t.getType(), msgTitle, text);
        }
    }

    private void createAndSendNotification(String sender, TRANSITION_TYPE transitionType,
                                           String title, String text) {
        String receiver = null;
        if (transitionType == TRANSITION_TYPE.ADMIN && !sender.equals(computation.getCreatorId())) {
            receiver = computation.getCreatorId();
        } else if (transitionType == TRANSITION_TYPE.USER && sender.equals(computation.getCreatorId())) {
            receiver = computation.getUserId();
        }

        if (receiver != null) {
            Notification notification = new Notification(
                    sender,
                    receiver,
                    netModel.getNetName(),
                    -1,
                    title,
                    text,
                    System.currentTimeMillis() / 1000
            );
            NotificationsDAO.insertNotification(notification);
        }
    }

    private String getNotificationText(boolean isFinished, String sender,
                                       String firedTransition, String newPossibleTransition) {
        if (isFinished) {
            return sender + " reached the finish node by firing " + firedTransition + ".";
        }
        if (firedTransition.isEmpty()) {
            return "Now you can fire '" + newPossibleTransition + "'.";
        }
        return sender + " fired '" + firedTransition + "', now you can fire '" + newPossibleTransition + "'.";
    }

    private int computeNextStepType(List<Transition> transitions) {
        boolean isNextUser = transitions.stream().anyMatch(t -> t.getType() == TRANSITION_TYPE.USER);
        boolean isNextAdmin = transitions.stream().anyMatch(t -> t.getType() == TRANSITION_TYPE.ADMIN);

        if (isNextUser && isNextAdmin) return 3;
        if (isNextAdmin) return 2;
        if (isNextUser) return 1;
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
}