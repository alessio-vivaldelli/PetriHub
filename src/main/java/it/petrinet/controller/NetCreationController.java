package it.petrinet.controller;

import it.petrinet.model.PetriNet;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.petrinet.view.PetriNetEditorPane;
import it.petrinet.service.SessionContext;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.toolbar.EditorToolBar;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Controller for the Petri Net creation and editing interface.
 * Manages the canvas, toolbar, and save functionality for creating new Petri nets.
 *
 * The layout consists of:
 * - A canvas (PetriNetEditorPane) as the main drawing area
 * - A toolbar overlay positioned at the top-center
 * - A finish button overlay positioned at the top-right
 */
public class NetCreationController {

    private static final Logger LOGGER = Logger.getLogger(NetCreationController.class.getName());
    private static final String TIMEZONE_ID = "Europe/Rome";
    private static final String FILE_EXTENSION = ".pnml";
    private static final String FILE_TYPE = "image";

    // FXML Components
    @FXML private StackPane canvasContainer;
    @FXML private HBox toolbarContainer;
    @FXML private Button finishButton;

    // Application Components
    private PetriNetEditorPane canvas;
    private EditorToolBar toolbar;
    private String netName;

    // State flags
    private boolean isInitialized = false;
    private boolean isSaving = false;

    @FXML
    public void initialize() {
        // FXML components are available here, but we wait for initData to initialize everything

        LOGGER.info("NetCreationController FXML initialized");
    }

    /**
     * Initializes the controller with the specified net name and sets up all components.
     * This method should be called after FXML loading.
     *
     * @param netName The name of the Petri net to create
     * @throws IllegalArgumentException if netName is null or empty
     * @throws IllegalStateException if controller is already initialized
     */
    public void initData(String netName) {
        validateInitialization(netName);

        this.netName = netName;

        try {
            initializeComponents();
            this.isInitialized = true;
            LOGGER.info("NetCreationController initialized successfully for net: " + netName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize NetCreationController", e);
            showErrorDialog("Initialization Error", "Failed to initialize the editor. Please try again.");
        }
    }

    /**
     * Validates the initialization parameters and state.
     */
    private void validateInitialization(String netName) {
        if (isInitialized) {
            throw new IllegalStateException("Controller is already initialized");
        }

        if (netName == null || netName.trim().isEmpty()) {
            throw new IllegalArgumentException("Net name cannot be null or empty");
        }
    }

    /**
     * Initializes all components in the correct order.
     */
    private void initializeComponents() {
        setupCanvas();
        setupToolbar();
        setupFinishButton();
    }

    /**
     * Sets up the canvas component and adds it to the container.
     * The canvas is added as the first child to remain in the background.
     */
    private void setupCanvas() {
        try {
            canvas = new PetriNetEditorPane(netName);
            configureCanvasProperties();
            configureCanvasEventHandlers();

            canvasContainer.getChildren().addFirst(canvas);

            // Initialize canvas after it's in the scene graph
            Platform.runLater(() -> {
                try {
                    canvas.init();
                    LOGGER.fine("Canvas initialized successfully");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to initialize canvas", e);
                    showErrorDialog("Canvas Error", "Failed to initialize the drawing canvas.");
                }
            });

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup canvas", e);
            throw new RuntimeException("Canvas setup failed", e);
        }
    }

    /**
     * Configures canvas properties for proper sizing.
     */
    private void configureCanvasProperties() {
        canvas.prefWidthProperty().bind(canvasContainer.widthProperty());
        canvas.prefHeightProperty().bind(canvasContainer.heightProperty());
    }

    /**
     * Configures event handlers for the canvas.
     */
    private void configureCanvasEventHandlers() {
        canvas.setOnPetriNetSaved(event -> handlePetriNetSaved());
    }

    /**
     * Sets up the toolbar and adds it to the toolbar container.
     */
    private void setupToolbar() {
        try {
            toolbar = new EditorToolBar(canvas);
            toolbarContainer.getChildren().add(toolbar);
            LOGGER.fine("Toolbar setup completed");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to setup toolbar", e);
            throw new RuntimeException("Toolbar setup failed", e);
        }
    }

    /**
     * Configures the finish button event handler.
     */
    private void setupFinishButton() {
        finishButton.setOnAction(event -> handleFinishAction());
        LOGGER.fine("Finish button configured");
    }

    /**
     * Handles the finish button click event.
     */
    @FXML
    private void handleFinishAction() {
        if (!isInitialized) {
            LOGGER.warning("Attempted to finish before initialization");
            return;
        }

        if (isSaving) {
            LOGGER.info("Save operation already in progress");
            return;
        }

        try {
            initiateNetSave();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during finish action", e);
            showErrorDialog("Save Error", "An error occurred while saving the net.");
        }
    }

    /**
     * Initiates the net save operation.
     */
    private void initiateNetSave() {
        isSaving = true;
        finishButton.setDisable(true);

        try {
            canvas.saveNetAction();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to save net", e);
            showErrorDialog("Save Error", "Failed to save the Petri net. Please try again.");
            isSaving = false;
            finishButton.setDisable(false);
        }
    }

    /**
     * Handles the event when the Petri net is successfully saved.
     */
    private void handlePetriNetSaved() {
        try {
            savePetriNetToDatabase();
            cleanupResources();
            navigateToHome();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error after net save", e);
            showErrorDialog("Database Error", "The net was saved but there was an error updating the database.");
        } finally {
            isSaving = false;
            finishButton.setDisable(false);
        }
    }

    /**
     * Saves the Petri net information to the database.
     */
    private void savePetriNetToDatabase() {
        PetriNet petriNet = createPetriNetRecord();
        PetriNetsDAO.insertNet(petriNet);
        LOGGER.info("Petri net saved to database: " + netName);
    }

    /**
     * Creates a PetriNet record for database insertion.
     */
    private PetriNet createPetriNetRecord() {
        String username = SessionContext.getInstance().getUser().getUsername();
        long timestamp = ZonedDateTime.now(ZoneId.of(TIMEZONE_ID)).toEpochSecond();
        String filename = netName + FILE_EXTENSION;

        return new PetriNet(
                netName,
                username,
                timestamp,
                filename,
                FILE_TYPE,
                true
        );
    }

    /**
     * Cleans up resources before navigation.
     */
    private void cleanupResources() {
        try {
            if (canvas != null) {
                // Perform any necessary cleanup on canvas
                canvas = null;
            }
            if (toolbar != null) {
                toolbar = null;
            }
            LOGGER.fine("Resources cleaned up successfully");
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error during resource cleanup", e);
        }
    }

    /**
     * Navigates back to the home scene.
     */
    private void navigateToHome() {
        Platform.runLater(() -> {
            try {
                ViewNavigator.homeScene(false);
                LOGGER.info("Navigation to home completed");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to navigate to home", e);
            }
        });
    }

    /**
     * Shows an error dialog to the user.
     */
    private void showErrorDialog(String title, String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
        });
    }

    // =================================================================================
    // GETTERS FOR TESTING AND INTEGRATION
    // =================================================================================

    /**
     * Gets the canvas component.
     * @return The PetriNetEditorPane instance, or null if not initialized
     */
    public PetriNetEditorPane getCanvas() {
        return canvas;
    }

    /**
     * Gets the toolbar component.
     * @return The EditorToolBar instance, or null if not initialized
     */
    public EditorToolBar getToolbar() {
        return toolbar;
    }

    /**
     * Gets the finish button component.
     * @return The finish Button instance
     */
    public Button getFinishButton() {
        return finishButton;
    }

    /**
     * Gets the net name.
     * @return The name of the current net being edited
     */
    public String getNetName() {
        return netName;
    }

    /**
     * Checks if the controller is initialized.
     * @return true if initialized, false otherwise
     */
    public boolean isInitialized() {
        return isInitialized;
    }

    /**
     * Checks if a save operation is in progress.
     * @return true if saving, false otherwise
     */
    public boolean isSaving() {
        return isSaving;
    }
}