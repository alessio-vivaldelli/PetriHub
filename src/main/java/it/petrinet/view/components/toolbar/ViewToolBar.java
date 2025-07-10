package it.petrinet.view.components.toolbar;

import it.petrinet.controller.NetVisualController;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.utils.IconUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

/**
 * REFACTOR: Questa classe rappresenta la toolbar della vista, il cui contenuto
 * cambia in base allo stato della computazione. Implementa lo State Pattern,
 * dove i suoi metodi di configurazione vengono chiamati dal NetVisualController (il Context).
 */
public class ViewToolBar extends ToolBar {

    private final PetriNetViewerPane board;
    private final NetVisualController controller;

    public ViewToolBar(PetriNetViewerPane board, NetVisualController controller) {
        super();
        // REFACTOR: Le dipendenze vengono impostate come final per garantire che non cambino dopo la costruzione.
        this.board = board;
        this.controller = controller;
    }

    /**
     * Configura la toolbar per lo stato SUBSCRIBABLE (l'utente non è iscritto).
     */
    public void configureForSubscribable() {
        Button playButton = createPlayButton();
        IconUtils.changeIconColor(playButton, Color.GRAY); // L'icona viene resa grigia
        playButton.setDisable(true); // Il pulsante è disabilitato

        updateToolbar(
                createGap(),
                playButton,
                createGap(3)
        );
    }

    /**
     * Configura la toolbar per lo stato NOT_STARTED (l'utente è iscritto ma non ha avviato la rete).
     */
    public void configureForStartable() {
        Button play = createPlayButton();
        play.setDisable(controller.isCreator());
        updateToolbar(
                createGap(),
                play,
                createGap(3),
                createInfoButton()
        );
    }

    /**
     * Configura la toolbar per lo stato STARTED (la rete è in esecuzione).
     */
    public void configureForStarted() {
        Button subscribe = createUnsubscribeButton();
        subscribe.setDisable(controller.isCreator());
        updateToolbar(
                subscribe,
                createRestartButton(),
                createGap(3),
                createInfoButton()
        );
    }

    /**
     * REFACTOR: Metodo factory generico e riutilizzabile per la creazione di qualsiasi pulsante della toolbar.
     * Centralizza la logica di configurazione comune (azione, icona, tooltip).
     *
     * @param iconName Il nome del file dell'icona (es. "play.png").
     * @param tooltipText Il testo da mostrare al passaggio del mouse.
     * @param action L'azione da eseguire al click.
     * @return Un'istanza di Button configurata.
     */
    private Button createToolbarButton(String iconName, String tooltipText, EventHandler<ActionEvent> action) {
        Button button = new Button();
        button.setOnAction(action);
        configureButton(button, iconName, tooltipText); // Metodo ereditato da ToolBar
        return button;
    }

    // REFACTOR: I metodi di creazione specifici ora usano la factory generica, riducendo la duplicazione.
    private Button createRestartButton() {
        return createToolbarButton("restart.png", "Restart Petri Net", e -> controller.restartAction());
    }

    private Button createUnsubscribeButton() {
        return createToolbarButton("unsubscribe.png", "Unsubscribe from Petri Net", e -> controller.unsubscribeAction());
    }

    private Button createPlayButton() {
        return createToolbarButton("play.png", "Start Petri Net", e -> controller.startAction());
    }

    private Button createInfoButton() {
        // REFACTOR: Aggiornata la chiamata al metodo corretto del controller refattorizzato.
        return createToolbarButton("history.png", "Show/Hide History", e -> controller.toggleHistory());
    }

    /**
     * Metodo helper che pulisce la toolbar e la ripopola con i nuovi nodi (pulsanti, spazi, ecc.).
     * @param nodes I componenti della UI da aggiungere alla toolbar.
     */
    private void updateToolbar(Node... nodes) {
        this.getChildren().clear();
        this.getChildren().addAll(nodes);
        addStaticButtons(board); // Aggiunge pulsanti statici definiti nella classe padre ToolBar
    }



}