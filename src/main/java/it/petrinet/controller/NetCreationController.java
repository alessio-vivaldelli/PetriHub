package it.petrinet.controller;

import it.petrinet.petrinet.view.PetriNetCreationPane;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class NetCreationController {

    @FXML private VBox CanvasContainer;
    private PetriNetCreationPane Canvas;

    @FXML
    public void initialize() {
        Canvas = new PetriNetCreationPane("testNet", "description", true);
        CanvasContainer.getChildren().add(Canvas);
        Canvas.start();

        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(event -> {
            Canvas.init();
        });
        delay.play();
    }


}
