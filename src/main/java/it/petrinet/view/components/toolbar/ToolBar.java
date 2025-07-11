package it.petrinet.view.components.toolbar;

import it.petrinet.petrinet.view.AbstractPetriNetPane;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;

/**
 * Classe base astratta per tutte le toolbar delle Reti di Petri.
 * Definisce l'aspetto grafico comune (stile, hover) e fornisce metodi
 * di utilità per creare pulsanti e separatori.
 */
public abstract class ToolBar extends HBox {

    // --- Definizione degli Stili CSS come costanti Java ---
    private static final String TOOLBAR_STYLE =
            "-fx-background-color: linear-gradient(to bottom, #1e1e2e, #1E1E2E);" +
                    "-fx-background-radius: 30;" +
                    "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.35), 12, 0.4, 0, 4);" +
                    "-fx-border-color: rgba(205,214,244,0.15);" +
                    "-fx-border-width: 1;" +
                    "-fx-border-radius: 30;";

    protected static final String BUTTON_STYLE_NORMAL =
            "-fx-background-color: transparent;" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-width: 0;" +
                    "-fx-padding: 8;" +
                    "-fx-cursor: hand;" +
                    "-fx-transition: all 0.15s ease-in-out;";

    protected static final String BUTTON_STYLE_HOVER =
            "-fx-background-color: linear-gradient(to bottom, #1e1e2e, #282839);" +
                    "-fx-background-radius: 12;" +
                    "-fx-border-width: 0;" +
                    "-fx-padding: 8;" +
                    "-fx-effect: dropshadow(gaussian, rgba(137,180,250,0.15), 6, 0.2, 0, 0);" +
                    "-fx-cursor: hand;";

    protected static final String BUTTON_STYLE_SELECTED =
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
    protected ToolBar() {
        super();
        initializeToolbar();
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
        this.setPickOnBounds(false);
    }

    /**
     * Aggiunge i pulsanti statici (home, zoom) alla fine della toolbar
     */
    protected void addStaticButtons(AbstractPetriNetPane canvas) {
        Button homeButton = createHomeButton();
        Button zoomInButton = createZoomInButton(canvas);
        Button zoomOutButton = createZoomOutButton(canvas);

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
        home.setOnAction(e -> ViewNavigator.homeScene(false));
        configureButton(home, "home.png", "Return to Home");
        return home;
    }

    /**
     * Crea il pulsante zoom in
     */
    private Button createZoomInButton(AbstractPetriNetPane canvas) {
        Button zoomIn = new Button();
        zoomIn.setOnAction(e -> canvas.zoomInAction());
        configureButton(zoomIn, "zoom_in.png", "Zoom In");
        return zoomIn;
    }

    /**
     * Crea il pulsante zoom out
     */
    private Button createZoomOutButton(AbstractPetriNetPane canvas) {
        Button zoomOut = new Button();
        zoomOut.setOnAction(e -> canvas.zoomOutAction());
        configureButton(zoomOut, "zoom_out.png", "Zoom Out");
        return zoomOut;
    }

    /**
     * Configura un Button statico con icona, tooltip e stili
     */
    protected static void configureButton(Button button, String iconName, String tooltip) {
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

    protected Region createGap(int multiplier) {
        Region gap = new Region();
        gap.setPrefWidth(10 * multiplier);
        gap.setPrefHeight(40);
        HBox.setMargin(gap, new Insets(0, 20, 0, 0));
        return gap;
    }

    protected Region createGap() {
        return createGap(1);
    }

}