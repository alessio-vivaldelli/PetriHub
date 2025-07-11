package it.petrinet.view.components.toolbar;

import it.petrinet.controller.NetCreationController;
import it.petrinet.petrinet.view.PetriNetEditorPane;
import it.petrinet.utils.IconUtils;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;

public class EditorToolBar extends ToolBar {

    private PetriNetEditorPane canvas;
    private ToggleGroup toolToggleGroup;

    public EditorToolBar(NetCreationController controller) {
        super();
        this.canvas = controller.getCanvas();
        setupCreationButtons();
    }


    /**
     * Configura i pulsanti di creazione degli elementi della rete di Petri
     */
    private void setupCreationButtons() {
        // Pulisce i pulsanti e i separatori esistenti
        this.getChildren().clear();

        // Crea i toggle button per gli strumenti
        ToggleButton placeButton = createPlaceButton();
        ToggleButton transitionButton = createTransitionButton();
        ToggleButton arcButton = createArcButton();
        ToggleButton selectionButton = createSelectionButton();
        ToggleButton deleteButton = createDeleteButton();

        // Configura il gruppo di toggle
        setupToggleGroup(placeButton, transitionButton, arcButton, selectionButton, deleteButton);

        // Aggiunge i pulsanti alla toolbar
        this.getChildren().addAll(
                placeButton,
                transitionButton,
                arcButton,
                createGap(),
                selectionButton,
                deleteButton
        );

        // Aggiunge i pulsanti statici
        addStaticButtons(canvas);
    }


    /**
     * Configura il gruppo di toggle e i listener per i cambi di selezione
     */
    private void setupToggleGroup(ToggleButton... buttons) {
        toolToggleGroup = new ToggleGroup();
        toolToggleGroup.getToggles().addAll(buttons);

        toolToggleGroup.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
            double standard = IconUtils.DEFAULT_ICON_SIZE;

            // Reset del toggle precedentemente selezionato
            if (oldToggle instanceof ToggleButton oldButton) {
                oldButton.setStyle(BUTTON_STYLE_NORMAL);
                // Ripristina l'icona al colore normale
                try {
                    IconUtils.changeIconSize(oldButton, standard, standard);
                } catch (Exception e) {
                    // Se IconUtils fallisce, continua senza modificare il colore
                    System.err.println("Warning: Could not reset button color: " + e.getMessage());
                }
            }

            // Imposta il nuovo toggle selezionato o null se nessuno è selezionato
            if (newToggle instanceof ToggleButton newButton) {
                newButton.setStyle(BUTTON_STYLE_SELECTED);
                // Cambia il colore dell'icona per indicare la selezione
                try {
                    IconUtils.changeIconSize(newButton, standard * 1.2, standard * 1.2);
                } catch (Exception e) {
                    // Se IconUtils fallisce, continua senza modificare il colore
                    System.err.println("Warning: Could not set selected button color: " + e.getMessage());
                }
            } else {
                // Nessun pulsante selezionato - imposta modalità null o default
                canvas.setCurrentMode(null); // o canvas.setCurrentMode(PetriNetCreationPane.MODE.NONE) se hai questa modalità
                canvas.setCurrentNodeType(null); // se applicabile
            }
        });
    }


    // Creation Buttons ---------------------------------------------


    /**
     * Crea il pulsante per la creazione di places
     */
    private ToggleButton createPlaceButton() {
        ToggleButton place = new ToggleButton();
        place.setOnAction(e -> {
            canvas.setCurrentMode(PetriNetEditorPane.MODE.CREATE);
            canvas.setCurrentNodeType(PetriNetEditorPane.NODE_TYPE.PLACE);
        });
        configureToggleButton(place, "Place.png", "Create Palace");
        return place;
    }

    /**
     * Crea il pulsante per la creazione di transizioni
     */
    private ToggleButton createTransitionButton() {
        ToggleButton transition = new ToggleButton();
        transition.setOnAction(e -> {
            canvas.setCurrentMode(PetriNetEditorPane.MODE.CREATE);
            canvas.setCurrentNodeType(PetriNetEditorPane.NODE_TYPE.TRANSITION);
        });
        configureToggleButton(transition, "Transition.png", "Create Transition");
        return transition;
    }

    /**
     * Crea il pulsante per la creazione di archi
     */
    private ToggleButton createArcButton() {
        ToggleButton arc = new ToggleButton();
        arc.setOnAction(e -> {
            canvas.setCurrentMode(PetriNetEditorPane.MODE.CONNECT);
            // Nota: implementare la logica specifica per gli archi se necessario
        });
        configureToggleButton(arc, "ConnectMode.png", "Create Arc");
        return arc;
    }

    /**
     * Crea il pulsante per la modalità di selezione
     */
    private ToggleButton createSelectionButton() {
        ToggleButton selection = new ToggleButton();
        selection.setOnAction(e -> canvas.setCurrentMode(PetriNetEditorPane.MODE.SELECTION));
        configureToggleButton(selection, "SelectMode.png", "Select Mode");
        return selection;
    }

    /**
     * Crea il pulsante per la modalità di cancellazione
     */
    private ToggleButton createDeleteButton() {
        ToggleButton delete = new ToggleButton();
        delete.setOnAction(e -> canvas.setCurrentMode(PetriNetEditorPane.MODE.DELETION));
        configureToggleButton(delete, "DeleteMode.png", "Delete Mode");
        return delete;
    }


    /**
     * Configura un ToggleButton con icona, tooltip e stili
     */
    private void configureToggleButton(ToggleButton button, String iconName, String tooltip) {
        // Imposta l'icona
        IconUtils.setIcon(button, iconName);

        // Imposta il tooltip
        button.setTooltip(new javafx.scene.control.Tooltip(tooltip));

        // Applica lo stile iniziale
        button.setStyle(BUTTON_STYLE_NORMAL);

        // Configura gli eventi di hover solo per i toggle non selezionati
        button.setOnMouseEntered(e -> {
            if (!button.isSelected()) {
                button.setStyle(BUTTON_STYLE_HOVER);
            }
        });

        button.setOnMouseExited(e -> {
            if (!button.isSelected()) {
                button.setStyle(BUTTON_STYLE_NORMAL);
            }
        });

        // Configura le dimensioni
        button.setPrefSize(40, 40);
        button.setMinSize(40, 40);
        button.setMaxSize(40, 40);
    }



}
