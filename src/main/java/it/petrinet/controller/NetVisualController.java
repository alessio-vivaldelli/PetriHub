package it.petrinet.controller;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.view.components.toolbar.ViewToolBar;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

public class NetVisualController implements Initializable {

    private PetriNetViewerPane board;

    @FXML private VBox boardContainer = new VBox();
    @FXML private HBox toolbarContainer;

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

        board.setOnPetriNetFinished( () -> {
            System.out.println("Petri net finished.");
        });

        board.setOnTransitionFired((transitionName, newMarkingState, newTransition) -> {
            System.out.println("User click: " + transitionName + ", new marking state: " + newMarkingState
                    + ", new firable transition: " + newTransition);
        });

        boardContainer.getChildren().add(board);

        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(_ -> {
            board.init();
        });
        delay.play();
    }

    //TODO: Implement the logic to retrieve the Computation object from the database
    private Computation getComputationFromDB() {
        //TODO: Replace with actual database retrieval logic
        Computation computation = new Computation("testnet", "creatorID", "userID");
        computation.addStep(new ComputationStep(1, 1, "testnet", "", "start_e:1", 123456));

        return computation;
    }

    private void setupToolbar() {
        ViewToolBar toolbar = new ViewToolBar(board);
        toolbarContainer.getChildren().add(toolbar);
    }


    // Additional methods for handling user interactions, updating the view, etc. can be added here
}
