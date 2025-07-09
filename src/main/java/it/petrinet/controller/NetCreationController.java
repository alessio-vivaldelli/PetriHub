package it.petrinet.controller;

import it.petrinet.model.PetriNet;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.petrinet.view.PetriNetEditorPane;
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
 * Implements the Command pattern for user actions and Observer pattern for event handling.
 * Uses Template Method pattern for initialization sequence.
 */
public class NetCreationController {

    private static final Logger LOGGER = Logger.getLogger(NetCreationController.class.getName());

    // Configuration constants
    private static final class Config {
        static final String TIMEZONE_ID = "Europe/Rome";
        static final String FILE_EXTENSION = ".pnml";
        static final String FILE_TYPE = "image";

        private Config() {} // Utility class
    }

    // FXML Components
    @FXML private StackPane canvasContainer;
    @FXML private HBox toolbarContainer;
    @FXML private Button finishButton;

    // Application Components
    private PetriNetEditorPane canvas;
    private EditorToolBar toolbar;
    private String netName;

    // State management
    private final ControllerState state = new ControllerState();

    // Event handlers
    private final SaveEventHandler saveEventHandler = new SaveEventHandler();
    private final FinishActionHandler finishActionHandler = new FinishActionHandler();

    @FXML
    public void initialize() {
        LOGGER.info("NetCreationController FXML initialized");
    }

    /**
     * Initializes the controller with the specified net name.
     * Template method that orchestrates the initialization sequence.
     *
     * @param netName The name of the Petri net to create
     * @throws IllegalArgumentException if netName is null or empty
     * @throws IllegalStateException if controller is already initialized
     */
    public void initData(String netName) {
        validateInitializationPreconditions(netName);

        this.netName = netName;

        try {
            executeInitializationSequence();
            state.setInitialized(true);
            LOGGER.info("NetCreationController initialized successfully for net: " + netName);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize NetCreationController", e);
            DialogHelper.showError("Initialization Error", "Failed to initialize the editor. Please try again.");
        }
    }

    /**
     * Template method for initialization sequence.
     */
    private void executeInitializationSequence() {
        initializeCanvas();
        initializeToolbar();
        initializeFinishButton();
    }

    /**
     * Validates preconditions for initialization.
     */
    private void validateInitializationPreconditions(String netName) {
        if (state.isInitialized()) {
            throw new IllegalStateException("Controller is already initialized");
        }

        if (netName == null || netName.trim().isEmpty()) {
            throw new IllegalArgumentException("Net name cannot be null or empty");
        }
    }

    /**
     * Factory method for canvas creation and setup.
     */
    private void initializeCanvas() {
        try {
            canvas = createCanvas();
            configureCanvas();
            addCanvasToContainer();
            scheduleCanvasInitialization();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize canvas", e);
            throw new RuntimeException("Canvas initialization failed", e);
        }
    }

    private PetriNetEditorPane createCanvas() {
        return new PetriNetEditorPane(netName);
    }

    private void configureCanvas() {
        canvas.prefWidthProperty().bind(canvasContainer.widthProperty());
        canvas.prefHeightProperty().bind(canvasContainer.heightProperty());
        canvas.setOnPetriNetSaved(e -> saveEventHandler.handlePetriNetSaved());
    }

    private void addCanvasToContainer() {
        canvasContainer.getChildren().addFirst(canvas);
    }

    private void scheduleCanvasInitialization() {
        Platform.runLater(() -> {
            try {
                canvas.init();
                LOGGER.fine("Canvas initialized successfully");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to initialize canvas", e);
                DialogHelper.showError("Canvas Error", "Failed to initialize the drawing canvas.");
            }
        });
    }

    /**
     * Factory method for toolbar creation and setup.
     */
    private void initializeToolbar() {
        try {
            toolbar = new EditorToolBar(canvas);
            toolbarContainer.getChildren().add(toolbar);
            LOGGER.fine("Toolbar initialized successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize toolbar", e);
            throw new RuntimeException("Toolbar initialization failed", e);
        }
    }

    /**
     * Configures the finish button with its event handler.
     */
    private void initializeFinishButton() {
        finishButton.setOnAction(e -> finishActionHandler.handleFinishAction());
        LOGGER.fine("Finish button configured");
    }

    /**
     * Command pattern implementation for finish action.
     */
    private class FinishActionHandler {

        void handleFinishAction() {
            if (!state.isInitialized()) {
                LOGGER.warning("Attempted to finish before initialization");
                return;
            }

            if (state.isSaving()) {
                LOGGER.info("Save operation already in progress");
                return;
            }

            try {
                if (DialogHelper.showConfirmation()) {
                    executeSaveCommand();
                }
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during finish action", e);
                DialogHelper.showError("Save Error", "An error occurred while saving the net.");
            }
        }

        private void executeSaveCommand() {
            state.setSaving(true);
            finishButton.setDisable(true);

            try {
                canvas.saveNetAction();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to save net", e);
                DialogHelper.showError("Save Error", "Failed to save the Petri net. Please try again.");
                resetSaveState();
            }
        }

        private void resetSaveState() {
            state.setSaving(false);
            finishButton.setDisable(false);
        }
    }

    /**
     * Observer pattern implementation for save events.
     */
    private class SaveEventHandler {

        void handlePetriNetSaved() {
            try {
                savePetriNetToDatabase();
                cleanupResources();
                navigateToHome();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error after net save", e);
                DialogHelper.showError("Database Error", "The net was saved but there was an error updating the database.");
            } finally {
                resetSaveState();
            }
        }

        private void savePetriNetToDatabase() {
            PetriNet petriNet = PetriNetFactory.createPetriNet(
                    netName,
                    ViewNavigator.getAuthenticatedUser().getUsername(),
                    ZonedDateTime.now(ZoneId.of(Config.TIMEZONE_ID)).toEpochSecond()
            );

            PetriNetsDAO.insertNet(petriNet);
            LOGGER.info("Petri net saved to database: " + netName);
        }

        private void cleanupResources() {
            try {
                canvas = null;
                toolbar = null;
                LOGGER.fine("Resources cleaned up successfully");
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error during resource cleanup", e);
            }
        }

        private void navigateToHome() {
            Platform.runLater(() -> {
                try {
                    ViewNavigator.exitPetriNetScene();
                    LOGGER.info("Navigation to home completed");
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Failed to navigate to home", e);
                }
            });
        }

        private void resetSaveState() {
            state.setSaving(false);
            finishButton.setDisable(false);
        }
    }

    /**
     * Factory for creating PetriNet objects.
     */
    private static class PetriNetFactory {
        static PetriNet createPetriNet(String netName, String username, long timestamp) {
            String filename = netName + Config.FILE_EXTENSION;
            return new PetriNet(netName, username, timestamp, filename, Config.FILE_TYPE, true);
        }
    }

    /**
     * Utility class for dialog operations.
     */
    private static class DialogHelper {

        static boolean showConfirmation() {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Save Petri Net");
            alert.setHeaderText("Save and finish editing?");
            alert.setContentText("This will save the current Petri net and return to the home screen.");

            Optional<ButtonType> result = alert.showAndWait();
            return result.isPresent() && result.get() == ButtonType.OK;
        }

        static void showError(String title, String message) {
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(title);
                alert.setHeaderText(null);
                alert.setContentText(message);
                alert.showAndWait();
            });
        }
    }

    /**
     * Encapsulates controller state management.
     */
    private static class ControllerState {
        private boolean initialized = false;
        private boolean saving = false;

        boolean isInitialized() { return initialized; }
        boolean isSaving() { return saving; }

        void setInitialized(boolean initialized) { this.initialized = initialized; }
        void setSaving(boolean saving) { this.saving = saving; }
    }

    // =================================================================================
    // PUBLIC API FOR TESTING AND INTEGRATION
    // =================================================================================

    public PetriNetEditorPane getCanvas() { return canvas; }
    public EditorToolBar getToolbar() { return toolbar; }
    public Button getFinishButton() { return finishButton; }
    public String getNetName() { return netName; }
    public boolean isInitialized() { return state.isInitialized(); }
    public boolean isSaving() { return state.isSaving(); }
}