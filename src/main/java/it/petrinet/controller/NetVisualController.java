package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.ResourceBundle;

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
        boardContainer.getChildren().add(board);

        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(evt -> board.init());
        delay.play();
    }

    private Button overlaySubscribeButton() {
        Button subscribe = new Button("Subscribe");
        subscribe.setMaxWidth(350);
        subscribe.setStyle("-fx-background-color: #89b4fa; -fx-text-fill: #1E1E2E; -fx-font-size: 16; -fx-background-radius: 8; -fx-padding: 10;");
        subscribe.setOnAction(e -> {
            try {
                handleSubscribe();
            } catch (InputTypeException ex) {
                throw new RuntimeException(ex);
            }
        });
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

    private void handleSubscribe() throws InputTypeException {
        // Create fade-out transition
        ComputationsDAO.insertComputation(
                new Computation(
                        netModel.getNetName(),
                        netModel.getCreatorId(),
                        ViewNavigator.getAuthenticatedUser().getUsername()
                )
        );

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
            try {
                ComputationStepDAO.removeAllStepsByComputation(computation);
                ComputationsDAO.setAsStarted(computation, System.currentTimeMillis());
                //TODO: Update computation
                board.setComputation(computation);
                toolbar.startableButton();
            } catch (InputTypeException e) {
                EnhancedAlert.showError("Error restarting Petri Net", "An error occurred while restarting the Petri Net: " + e.getMessage());
            }
        }

    }

    public void unsubscribeAction() {
        Optional<EnhancedAlert.AlertResult> result = EnhancedAlert.showConfirmation(
                "Unsubscribe from Petri Net",
                "Are you sure you want to unsubscribe from the Petri Net?\n This will remove your subscription and reset your computation."
        );

        if(result.get().isCancel()) return;

        if( result.get().isOK()) {
            try {
                ComputationStepDAO.removeAllStepsByComputation(computation);
                //ComputationsDAO.unsubscribeFromNet(computation, ViewNavigator.getAuthenticatedUser().getUsername());
                board.setComputation(null);
                toolbar.subButton();
            } catch (InputTypeException e) {
                EnhancedAlert.showError("Error unsubscribing from Petri Net", "An error occurred while unsubscribing: " + e.getMessage());
            }
        }

    }

    public void startAction() throws InputTypeException {
        ComputationStep step = new ComputationStep(
                ComputationsDAO.getIdByComputation(computation),
                netModel.getNetName(),
                board.getStartPlaceName(),
                board.getStartPlaceName() + ":1",
                System.currentTimeMillis()/1000
        );

        computation.addStep(step);
        board.setComputation(computation);
        ComputationsDAO.insertComputation(step);


    }

    private static String getNetPath(PetriNet netModel) {
        return System.getProperty("user.dir") + "/src/main/resources/data/pnml/" + netModel.getXML_PATH();
    }


}
