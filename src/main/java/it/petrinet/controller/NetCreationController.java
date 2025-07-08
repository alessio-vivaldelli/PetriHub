package it.petrinet.controller;

import it.petrinet.petrinet.view.PetriNetEditorPane;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.toolbar.EditorToolBar;
import it.petrinet.view.components.toolbar.ToolBar;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller per l'editor delle Reti di Petri
 * La toolbar e i pulsanti sono sempre visibili sopra il canvas
 */
public class NetCreationController implements Initializable {

    public Button finishButton;

    @FXML private VBox canvasContainer;
    @FXML private HBox toolbarContainer;

    private PetriNetEditorPane canvas;
    private EditorToolBar toolbar;
    private static String netName;

    public static void setNetName(String netName) {
        NetCreationController.netName = netName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCanvas();
        setupToolbar();
    }

    /**
     * Inizializza il canvas PetriNetCreationPane
     */
    private void setupCanvas() {
        canvas = new PetriNetEditorPane(netName);

        // Il canvas deve occupare tutto lo spazio disponibile
        canvas.prefWidthProperty().bind(canvasContainer.widthProperty());
        canvas.prefHeightProperty().bind(canvasContainer.heightProperty());

        canvasContainer.getChildren().add(canvas);

        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(_ -> {
            canvas.init();
        });
        delay.play();
    }

    /**
     * Crea e configura la toolbar personalizzata
     */
    private void setupToolbar() {
        toolbar = new EditorToolBar(canvas);
        toolbarContainer.getChildren().add(toolbar);
    }

    /**
     * Gestisce il click sul pulsante Finish
     */
    @FXML
    private void onFinishAction() {
        System.out.println("Finish button clicked - Saving the Petri net");

        canvas.saveNetAction();

        // Clean up any resources
        if (canvas != null) canvas = null;
        if (toolbar != null) toolbar = null;


        // Navigate back to home
        ViewNavigator.exitPetriNet();

    }


    /**
     * Getter per il canvas (utile per test o integrazioni esterne)
     */
    public PetriNetEditorPane getCanvas() {
        return canvas;
    }

    /**
     * Getter per la toolbar (utile per test o integrazioni esterne)
     */
    public ToolBar getToolbar() {
        return toolbar;
    }
}