package it.petrinet.controller;

import it.petrinet.petrinet.view.PetriNetCreationPane;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;

public class NetCreationController {

    @FXML private VBox CanvasContainer;
    private PetriNetCreationPane Canvas;

    @FXML
    public void initialize() {
        Canvas = new PetriNetCreationPane("testNet", "description", true);
        CanvasContainer.getChildren().add(Canvas);
        Canvas.start();
    }

}
