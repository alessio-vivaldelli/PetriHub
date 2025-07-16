package it.petrinet.view.components.toolbar;

import it.petrinet.controller.NetVisualController;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import it.petrinet.utils.IconUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

/**
 * REFACTOR: Questa classe rappresenta la toolbar della vista, il cui contenuto
 * cambia in base allo stato della computazione. Implementa lo State Pattern,
 * dove i suoi metodi di configurazione vengono chiamati dal NetVisualController (il Context).
 */
public class ViewToolBar extends ToolBar {

    private final PetriNetViewerPane board;
    private final NetVisualController controller;

    // Cache per i pulsanti per evitare ricreazioni inutili
    private final Map<ButtonType, Button> buttonCache = new HashMap<>();

    // Enum per identificare i tipi di pulsante
    private enum ButtonType {
        PLAY("play.png", "Start Petri Net"),
        UNSUBSCRIBE("unsubscribe.png", "Unsubscribe from Petri Net"),
        RESTART("restart.png", "Restart Petri Net"),
        INFO("history.png", "Show/Hide History");

        private final String iconName;
        private final String tooltipText;

        ButtonType(String iconName, String tooltipText) {
            this.iconName = iconName;
            this.tooltipText = tooltipText;
        }

        public String getIconName() { return iconName; }
        public String getTooltipText() { return tooltipText; }
    }

    public ViewToolBar(NetVisualController controller) {
        super();
        this.board = controller.getBoard();
        this.controller = controller;
        initializeButtonCache();
    }

    /**
     * Inizializza la cache dei pulsanti per evitare ricreazioni multiple
     */
    private void initializeButtonCache() {
        buttonCache.put(ButtonType.PLAY, createCachedButton(ButtonType.PLAY, e -> controller.startAction()));
        buttonCache.put(ButtonType.UNSUBSCRIBE, createCachedButton(ButtonType.UNSUBSCRIBE, e -> controller.unsubscribeAction()));
        buttonCache.put(ButtonType.RESTART, createCachedButton(ButtonType.RESTART, e -> controller.restartAction()));
        buttonCache.put(ButtonType.INFO, createCachedButton(ButtonType.INFO, e -> controller.toggleHistory()));
    }

    /**
     * Configura la toolbar per lo stato SUBSCRIBABLE (l'utente non è iscritto).
     */
    public void configureForSubscribable() {
        Button playButton = getButton(ButtonType.PLAY);
        Button unSubscribeButton = getButton(ButtonType.UNSUBSCRIBE);

        configureDisabledButton(playButton);
        configureDisabledButton(unSubscribeButton);

        updateToolbar(
                unSubscribeButton,
                playButton,
                createGap(3)
        );
    }

    /**
     * Configura la toolbar per lo stato NOT_STARTED (l'utente è iscritto ma non ha avviato la rete).
     */
    public void configureForStartable() {
        Button playButton = getButton(ButtonType.PLAY);
        Button unSubscribeButton = getButton(ButtonType.UNSUBSCRIBE);
        Button infoButton = getButton(ButtonType.INFO);

        boolean isCreator = controller.isCreator();
        configureButtonState(playButton, isCreator);
        configureButtonState(unSubscribeButton, isCreator);
        configureButtonState(infoButton, false); // Info button is always enabled

        updateToolbar(
                unSubscribeButton,
                playButton,
                createGap(3),
                infoButton
        );
    }

    /**
     * Configura la toolbar per lo stato STARTED (la rete è in esecuzione).
     */
    public void configureForStarted() {
        Button unSubscribeButton = getButton(ButtonType.UNSUBSCRIBE);
        Button restartButton = getButton(ButtonType.RESTART);
        Button infoButton = getButton(ButtonType.INFO);

        boolean isCreator = controller.isCreator();
        configureButtonState(unSubscribeButton, isCreator);
        configureButtonState(restartButton, false); // Restart is always enabled when started
        configureButtonState(infoButton, false); // Info button is always enabled

        updateToolbar(
                unSubscribeButton,
                restartButton,
                createGap(3),
                infoButton
        );
    }

    /**
     * Ottiene un pulsante dalla cache, ripristinando il suo stato predefinito
     */
    private Button getButton(ButtonType type) {
        Button button = buttonCache.get(type);
        resetButtonToDefaultState(button);
        return button;
    }

    /**
     * Ripristina un pulsante al suo stato predefinito (abilitato, colore normale)
     */
    private void resetButtonToDefaultState(Button button) {
        button.setDisable(false);
        // Ripristina il colore dell'icona al colore predefinito
        IconUtils.removeColorEffect(button);
    }

    /**
     * Configura un pulsante come disabilitato con icona grigia
     */
    private void configureDisabledButton(Button button) {
        button.setDisable(true);
        IconUtils.changeIconColor(button, Color.GRAY);
    }

    /**
     * Configura lo stato di un pulsante basandosi sulla condizione di disabilitazione
     */
    private void configureButtonState(Button button, boolean shouldDisable) {
        if (shouldDisable) {
            configureDisabledButton(button);
        }
        // Se non deve essere disabilitato, mantiene lo stato predefinito già impostato da resetButtonToDefaultState
    }

    /**
     * Crea un pulsante per la cache con configurazione completa
     */
    private Button createCachedButton(ButtonType type, EventHandler<ActionEvent> action) {
        Button button = new Button();
        button.setOnAction(action);
        configureButton(button, type.getIconName(), type.getTooltipText());
        return button;
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