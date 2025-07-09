package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.PetriNet;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.petrinet.view.PetriNetEditorPane;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.toolbar.EditorToolBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controller per l'editor delle Reti di Petri
 * Il canvas è in fondo; toolbar e finishButton stanno in overlay sopra.
 */
public class NetCreationController implements Initializable {

    private PetriNetEditorPane canvas;
    private EditorToolBar      toolbar;

    @FXML
    private StackPane canvasContainer;   // definito in FXML
    @FXML
    private HBox      toolbarContainer;  // dichiarato in FXML come figlio di canvasContainer
    @FXML
    private Button    finishButton;      // dichiarato in FXML come figlio di canvasContainer

    private static String netName;

    public static void setNetName(String netName) {
        NetCreationController.netName = netName;
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupCanvas();
        setupToolbar();        // popola toolbarContainer, ma NON lo aggiunge a canvasContainer
        setupFinishButton();   // configura finishButton, ma NON lo aggiunge a canvasContainer
    }

    /** 1) Monta e initia il canvas in fondo allo StackPane. */
    private void setupCanvas() {
        canvas = new PetriNetEditorPane(netName);
        canvas.prefWidthProperty().bind(canvasContainer.widthProperty());
        canvas.prefHeightProperty().bind(canvasContainer.heightProperty());

        canvas.setOnPetriNetSaved(e -> {

            try {
                PetriNetsDAO.insertNet(
                        new PetriNet(
                                netName,
                                ViewNavigator.getAuthenticatedUser().getUsername(),
                                System.currentTimeMillis()/1000,
                                netName + ".pnml",
                                "image",
                                true
                        )
                );
            } catch (InputTypeException ex) {
                throw new RuntimeException(ex);
            }

            // Clean up any resources
            if (canvas != null) canvas = null;
            if (toolbar != null) toolbar = null;

            // Navigate back to home
            ViewNavigator.exitPetriNet();
        });

        // Aggiungo il canvas come primo figlio: rimane in fondo
        canvasContainer.getChildren().addFirst(canvas);

        // init del canvas *dopo* che è in scena
        Platform.runLater(canvas::init);
    }

    /** 2) Popola la toolbar esistente (posizionata in alto-centro da FXML). */
    private void setupToolbar() {
        toolbar = new EditorToolBar(canvas);
        toolbarContainer.getChildren().add(toolbar);
        // Allineamento e dimensioni gestiti in FXML con:
        //   StackPane.alignment="TOP_CENTER"
        //   prefHeight/maxHeight/minHeight nel FXML della HBox
    }

    /** 3) Configura il pulsante Finish esistente (posizionato in alto-destra da FXML). */
    private void setupFinishButton() {
        finishButton.setOnAction(e -> onFinishAction());
        // Allineamento/margini gestiti in FXML con:
        //   StackPane.alignment="TOP_RIGHT"
        //   <StackPane.margin><Insets .../></StackPane.margin>
    }

    /**
     * Gestisce il click sul pulsante Finish
     */
    @FXML
    private void onFinishAction() {
        canvas.saveNetAction();
    }

    // getters per test/integrazioni
    public PetriNetEditorPane getCanvas()      { return canvas; }
    public EditorToolBar      getToolbar()     { return toolbar; }
    public Button             getFinishButton(){ return finishButton; }
}
