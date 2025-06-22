package it.petrinet.view.components;

import it.petrinet.petrinet.view.PetriNetCreationPane;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

/**
 * Classe base astratta per tutte le toolbar delle Reti di Petri.
 * Definisce l'aspetto grafico comune (stile, hover) e fornisce metodi
 * di utilità per creare pulsanti e separatori.
 */
public class ToolBar extends HBox {

    protected final PetriNetCreationPane canvas;
    private ToggleGroup toolToggleGroup;

    // --- Definizione degli Stili CSS come costanti Java ---
private static final String TOOLBAR_STYLE =
                    "-fx-background-color: linear-gradient(to bottom, #1e1e2e, #1E1E2E);" +
                            "-fx-background-radius: 30;" +
                            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0.4, 0, 4);" +
                            "-fx-border-color: rgba(205,214,244,0.15);" +
                            "-fx-border-width: 1;" +
                            "-fx-border-radius: 30;";

            private static final String BUTTON_STYLE_NORMAL =
                    "-fx-background-color: transparent;" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 8;" +
                            "-fx-cursor: hand;" +
                            "-fx-transition: all 0.15s ease-in-out;";

            private static final String BUTTON_STYLE_HOVER =
                    "-fx-background-color: linear-gradient(to bottom, #1e1e2e, #282839);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 8;" +
                            "-fx-effect: dropshadow(gaussian, rgba(137,180,250,0.15), 6, 0.2, 0, 0);" +
                            "-fx-cursor: hand;";

            private static final String BUTTON_STYLE_SELECTED =
                    "-fx-background-color: linear-gradient(to bottom, #282839, #313244);" +
                            "-fx-background-radius: 12;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 8;" +
                            "-fx-effect: innershadow(gaussian, rgba(30,30,46,0.2), 1, 0, 0, 1), " +
                            "dropshadow(gaussian, rgba(180,190,254,0.2), 5, 0.3, 0, 0);" +
                            "-fx-cursor: hand;";
/**
     * Costruttore protetto.
     * Applica lo stile e il layout di base alla toolbar (HBox).
     */
public ToolBar(PetriNetCreationPane canvas) {
        super();
        this.canvas = canvas;
        initializeToolbar();
        setupCreationButtons();
    }

    /**
     * Inizializza lo stile e il layout di base della toolbar
     */
    private void initializeToolbar() {
        this.setStyle(TOOLBAR_STYLE);
        this.setAlignment(Pos.CENTER);
        this.setPadding(new Insets(8, 16, 8, 16));
        this.setSpacing(4);
        this.setMinHeight(50);
        this.setPrefHeight(50);
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
        addStaticButtons();
    }

    /**
     * Crea il pulsante per la creazione di places
     */
    private ToggleButton createPlaceButton() {
        ToggleButton place = new ToggleButton();
        place.setOnAction(e -> {
            canvas.setCurrentMode(PetriNetCreationPane.MODE.CREATE);
            canvas.setCurrentNodeType(PetriNetCreationPane.NODE_TYPE.PLACE);
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
            canvas.setCurrentMode(PetriNetCreationPane.MODE.CREATE);
            canvas.setCurrentNodeType(PetriNetCreationPane.NODE_TYPE.TRANSITION);
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
            canvas.setCurrentMode(PetriNetCreationPane.MODE.CONNECT);
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
        selection.setOnAction(e -> canvas.setCurrentMode(PetriNetCreationPane.MODE.SELECTION));
        configureToggleButton(selection, "SelectMode.png", "Select Mode");
        return selection;
    }

    /**
     * Crea il pulsante per la modalità di cancellazione
     */
    private ToggleButton createDeleteButton() {
        ToggleButton delete = new ToggleButton();
        delete.setOnAction(e -> canvas.setCurrentMode(PetriNetCreationPane.MODE.DELETION));
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

            // Imposta il nuovo toggle selezionato
            if (newToggle instanceof ToggleButton newButton) {
                newButton.setStyle(BUTTON_STYLE_SELECTED);
                // Cambia il colore dell'icona per indicare la selezione
                try {
                    IconUtils.changeIconSize(newButton,standard*1.2, standard*1.2);
                } catch (Exception e) {
                    // Se IconUtils fallisce, continua senza modificare il colore
                    System.err.println("Warning: Could not set selected button color: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Aggiunge i pulsanti statici (home, zoom) alla fine della toolbar
     */
    private void addStaticButtons() {
        Button homeButton = createHomeButton();
        Button zoomInButton = createZoomInButton();
        Button zoomOutButton = createZoomOutButton();

        this.getChildren().addAll(
                createSeparator(),
                homeButton,
                zoomInButton,
                zoomOutButton
        );
    }

    /**
     * Crea il pulsante home
     */
    private Button createHomeButton() {
        Button home = new Button();
        home.setOnAction(e -> ViewNavigator.exitCreation());
        configureStaticButton(home, "home.png", "Return to Home");
        return home;
    }

    /**
     * Crea il pulsante zoom in
     */
    private Button createZoomInButton() {
        Button zoomIn = new Button();
        zoomIn.setOnAction(e -> canvas.zoomInAction());
        configureStaticButton(zoomIn, "zoom_in.png", "Zoom In");
        return zoomIn;
    }

    /**
     * Crea il pulsante zoom out
     */
    private Button createZoomOutButton() {
        Button zoomOut = new Button();
        zoomOut.setOnAction(e -> canvas.zoomOutAction());
        configureStaticButton(zoomOut, "zoom_out.png", "Zoom Out");
        return zoomOut;
    }

    /**
     * Configura un Button statico con icona, tooltip e stili
     */
    private void configureStaticButton(Button button, String iconName, String tooltip) {
        // Imposta l'icona
        IconUtils.setIcon(button, iconName);

        // Imposta il tooltip
        button.setTooltip(new javafx.scene.control.Tooltip(tooltip));

        // Applica lo stile
        button.setStyle(BUTTON_STYLE_NORMAL);

        // Configura gli eventi di hover
        button.setOnMouseEntered(e -> button.setStyle(BUTTON_STYLE_HOVER));
        button.setOnMouseExited(e -> button.setStyle(BUTTON_STYLE_NORMAL));

        // Configura le dimensioni
        button.setPrefSize(40, 40);
        button.setMinSize(40, 40);
        button.setMaxSize(40, 40);
    }

    /**
     * Metodo di utilità protetto per creare un separatore verticale.
     * @return una Region configurata per agire come separatore.
     */
    protected Region createSeparator() {
        Region separator = new Region();
        separator.setPrefWidth(1);
        separator.setPrefHeight(40);
        separator.setStyle("-fx-background-color: white;");
        HBox.setMargin(separator, new Insets(0, 8, 0, 8));
        return separator;
    }

    protected Region createGap(){
        Region gap = new Region();
        gap.setPrefWidth(10);
        gap.setPrefHeight(40);
        HBox.setMargin(gap, new Insets(0, 20, 0, 0));
        return gap;
    }


    /**
     * Getter per il gruppo di toggle (utile per sottoclassi)
     */
    protected ToggleGroup getToolToggleGroup() {
        return toolToggleGroup;
    }

    /**
     * Metodo per deselezionare tutti i toggle (utile per sottoclassi)
     */
    protected void clearSelection() {
        if (toolToggleGroup != null) {
            toolToggleGroup.selectToggle(null);
        }
    }

    /**
     * Metodo per selezionare programmaticamente un toggle
     */
    protected void selectTool(int index) {
        if (toolToggleGroup != null && index >= 0 && index < toolToggleGroup.getToggles().size()) {
            toolToggleGroup.selectToggle(toolToggleGroup.getToggles().get(index));
        }
    }
}