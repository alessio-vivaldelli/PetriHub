package it.petrinet.view.components;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Objects;
import java.util.Optional;

/**
 * Enhanced Alert Dialog with multiple dialog types, flexible content, and smooth animations.
 * Fixed layout issues and message truncation problems.
 * Now features dynamic sizing based on content, improved centering, and a more compact default size.
 */
public class EnhancedAlert {

    // Constants
    private static final String CSS_PATH = "/styles/style.css";
    private static final Duration ANIMATION_DURATION = Duration.millis(300); // Entry animation duration
    private static final Duration FAST_ANIMATION = Duration.millis(200);   // Exit animation duration

    // Sizing limits - Adjusted for a more compact default
    private static final double MIN_WIDTH = 320; // Slightly reduced min width
    private static final double MAX_WIDTH = 650; // Slightly reduced max width, still generous
    private static final double MIN_HEIGHT = 140; // Slightly reduced min height
    private static final double MAX_HEIGHT = 300; // Adjusted max height

    // Layout constants
    private static final double CONTENT_SPACING = 18; // Reduced spacing slightly
    private static final double BUTTON_SPACING = 15;
    private static final double DIALOG_PADDING = 25; // Reduced padding slightly

    // Enums
    public enum AlertType {
        INFORMATION, WARNING, ERROR, CONFIRMATION, TEXT_INPUT, CUSTOM
    }

    public enum ButtonType {
        OK, CANCEL, YES, NO, CLOSE
    }

    // AlertResult class
    public static class AlertResult {
        private final ButtonType buttonPressed;
        private final String textInput;

        public AlertResult(ButtonType buttonPressed, String textInput) {
            this.buttonPressed = buttonPressed;
            this.textInput = textInput;
        }

        public ButtonType getButtonPressed() {
            return buttonPressed;
        }

        public String getTextInput() {
            return textInput;
        }

        public boolean isOK() {
            return buttonPressed == ButtonType.OK;
        }

        public boolean isCancel() {
            return buttonPressed == ButtonType.CANCEL;
        }

        public boolean isYes() {
            return buttonPressed == ButtonType.YES;
        }

        public boolean isNo() {
            return buttonPressed == ButtonType.NO;
        }
    }

    // Instance variables
    private final Stage ownerStage;
    private final Stage popupStage;
    private final StackPane popupRoot;
    private final VBox contentContainer;
    private final VBox dialogContent;

    private AlertResult result;
    private static Stage defaultStage;

    // Constructor
    public EnhancedAlert(Stage ownerStage) {
        this.ownerStage = Objects.requireNonNull(ownerStage, "Owner Stage cannot be null");
        this.popupStage = new Stage();
        this.popupRoot = new StackPane();
        this.contentContainer = new VBox(CONTENT_SPACING);
        this.dialogContent = new VBox(CONTENT_SPACING);

        initializeComponents();
    }

    // Initialization
    private void initializeComponents() {
        setupPopupStage();
        setupLayout();
        setupStyling();
        setupEventHandlers();
    }

    private void setupPopupStage() {
        popupStage.initOwner(ownerStage);
        popupStage.initStyle(StageStyle.TRANSPARENT);
        bindToOwner();
    }

    private void setupLayout() {
        // Configure content container
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setPadding(new Insets(DIALOG_PADDING));
        contentContainer.setMaxWidth(Region.USE_PREF_SIZE); // Allows dynamic sizing based on content
        contentContainer.setMaxHeight(Region.USE_PREF_SIZE); // Allows dynamic sizing based on content

        // Configure dialog content - ensures elements inside are centered
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setFillWidth(true); // Allows children like labels to expand horizontally

        // Add dialog content to container
        contentContainer.getChildren().add(dialogContent);

        // Prevent event bubbling, so clicks on content don't close the dialog
        contentContainer.setOnMouseClicked(Event::consume);

        // Add container to root
        popupRoot.getChildren().add(contentContainer);
        StackPane.setAlignment(contentContainer, Pos.CENTER); // Ensure content container is centered in the root

        // Create and set scene
        Scene popupScene = new Scene(popupRoot, ownerStage.getWidth(), ownerStage.getHeight());
        popupScene.setFill(Color.TRANSPARENT);
        popupStage.setScene(popupScene);
    }

    private void setupStyling() {
        // Darker overlay for a more professional look
        popupRoot.setStyle("-fx-background-color: rgb(30,30,46,0.75);");
        contentContainer.getStyleClass().add("enhanced-alert-pane");

        try {
            if (getClass().getResource(CSS_PATH) != null) {
                popupStage.getScene().getStylesheets().add(
                        getClass().getResource(CSS_PATH).toExternalForm()
                );
            } else {
                System.err.println("Warning: " + CSS_PATH + " not found.");
            }
        } catch (Exception e) {
            System.err.println("Error loading CSS: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        // Keyboard handlers
        popupStage.setOnShown(event -> {
            popupStage.getScene().setOnKeyPressed(evt -> {
                if (evt.getCode() == KeyCode.ESCAPE) {
                    result = new AlertResult(ButtonType.CANCEL, null);
                    closeWithAnimation();
                }
            });
        });

        // Mouse handlers - click outside to close
        popupRoot.setOnMouseClicked(evt -> {
            if (evt.getTarget() == popupRoot) { // Only close if click is directly on the root overlay
                result = new AlertResult(ButtonType.CANCEL, null);
                closeWithAnimation();
            }
        });
    }

    private void bindToOwner() {
        // Bind popup stage size and position to owner stage
        popupStage.setX(ownerStage.getX());
        popupStage.setY(ownerStage.getY());
        popupStage.setWidth(ownerStage.getWidth());
        popupStage.setHeight(ownerStage.getHeight());

        ownerStage.xProperty().addListener((obs, oldVal, newVal) ->
                popupStage.setX(newVal.doubleValue()));
        ownerStage.yProperty().addListener((obs, oldVal, newVal) ->
                popupStage.setY(newVal.doubleValue()));
        ownerStage.widthProperty().addListener((obs, oldVal, newVal) ->
                popupStage.setWidth(newVal.doubleValue()));
        ownerStage.heightProperty().addListener((obs, oldVal, newVal) ->
                popupStage.setHeight(newVal.doubleValue()));
    }

    // Static factory methods for convenience
    public static void initDefaultStage(Stage stage) {
        defaultStage = stage;
    }

    private static void requireDefaultStage() {
        if (defaultStage == null) {
            throw new IllegalStateException(
                    "Default stage not initialized. Call EnhancedAlert.initDefaultStage(primaryStage) at startup."
            );
        }
    }

    public static Optional<AlertResult> showInformation(String title, String message) {
        requireDefaultStage();
        return new EnhancedAlert(defaultStage).createInformationAlert(title, message).showAndWait();
    }

    public static Optional<AlertResult> showWarning(String title, String message) {
        requireDefaultStage();
        return new EnhancedAlert(defaultStage).createWarningAlert(title, message).showAndWait();
    }

    public static Optional<AlertResult> showError(String title, String message) {
        requireDefaultStage();
        return new EnhancedAlert(defaultStage).createErrorAlert(title, message).showAndWait();
    }

    public static Optional<AlertResult> showConfirmation(String title, String message) {
        requireDefaultStage();
        return new EnhancedAlert(defaultStage).createConfirmationAlert(title, message).showAndWait();
    }

    public static Optional<AlertResult> showTextInput(String title, String message, String defaultText) {
        requireDefaultStage();
        return new EnhancedAlert(defaultStage).createTextInputAlert(title, message, defaultText).showAndWait();
    }

    // Alert Creation Methods
    public EnhancedAlert createInformationAlert(String title, String message) {
        Button okButton = createButton("OK", ButtonType.OK, true);
        buildDialogContent(title, message, "info-title", createButtonLayout(okButton));
        autoSizeForContent(message, false);
        return this;
    }

    public EnhancedAlert createWarningAlert(String title, String message) {
        Button okButton = createButton("OK", ButtonType.OK, true);
        buildDialogContent(title, message, "warning-title", createButtonLayout(okButton));
        autoSizeForContent(message, false);
        return this;
    }

    public EnhancedAlert createErrorAlert(String title, String message) {
        Button okButton = createButton("OK", ButtonType.OK, true);
        buildDialogContent(title, message, "error-title", createButtonLayout(okButton));
        autoSizeForContent(message, false);
        return this;
    }

    public EnhancedAlert createConfirmationAlert(String title, String message) {
        Button yesButton = createButton("Yes", ButtonType.YES, true);
        Button noButton = createButton("No", ButtonType.NO, false);
        HBox buttonBox = new HBox(BUTTON_SPACING, yesButton, noButton);
        buttonBox.setAlignment(Pos.CENTER);
        buildDialogContent(title, message, "info-title", buttonBox);
        autoSizeForContent(message, false);
        return this;
    }

    public EnhancedAlert createTextInputAlert(String title, String message, String defaultText) {
        Label titleLabel = createTitleLabel(title, "info-title");
        Label messageLabel = createMessageLabel(message);

        TextField textField = new TextField(defaultText != null ? defaultText : "");
        textField.getStyleClass().add("text-input-field");
        textField.setPrefWidth(280); // Adjusted fixed width for the text field for compactness

        Button okButton = createButton("OK", ButtonType.OK, true);
        Button cancelButton = createButton("Cancel", ButtonType.CANCEL, false);

        okButton.setOnAction(e -> {
            result = new AlertResult(ButtonType.OK, textField.getText());
            closeWithAnimation();
        });

        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                okButton.fire();
            }
        });

        HBox buttonBox = new HBox(BUTTON_SPACING, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        dialogContent.getChildren().setAll(titleLabel, messageLabel, textField, buttonBox);
        autoSizeForContent(message, true); // Indicate that it has a text input

        popupStage.setOnShown(e -> Platform.runLater(() -> {
            textField.requestFocus();
            textField.selectAll();
        }));
        return this;
    }

    // Helper methods for building UI components
    private void buildDialogContent(String title, String message, String titleStyleClass, Node buttonLayout) {
        Label titleLabel = createTitleLabel(title, titleStyleClass);
        Label messageLabel = createMessageLabel(message);
        dialogContent.getChildren().setAll(titleLabel, messageLabel, buttonLayout);
    }

    private HBox createButtonLayout(Button... buttons) {
        HBox buttonBox = new HBox(BUTTON_SPACING);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.getChildren().addAll(buttons);
        return buttonBox;
    }

    private Label createTitleLabel(String text, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().addAll("title-label", styleClass);
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER); // Center text within the label
        label.setMaxWidth(Double.MAX_VALUE); // Allow it to expand to fill width
        return label;
    }

    private Label createMessageLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("message-label");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER); // Center text within the label
        label.setMaxWidth(Double.MAX_VALUE); // Allow it to expand to fill width
        // Allow the message label to grow vertically if content is long
        VBox.setVgrow(label, javafx.scene.layout.Priority.SOMETIMES);
        return label;
    }

    private Button createButton(String text, ButtonType type, boolean isDefault) {
        Button button = new Button(text);
        button.setDefaultButton(isDefault);
        button.getStyleClass().addAll("dialog-button", getButtonStyleClass(type));

        button.setOnAction(e -> {
            result = new AlertResult(type, null);
            closeWithAnimation();
        });

        return button;
    }

    private String getButtonStyleClass(ButtonType type) {
        return switch (type) {
            case OK, YES -> "ok-button";
            case CANCEL, NO, CLOSE -> "cancel-button";
        };
    }

    /**
     * Dynamically sizes the alert based on message length and whether it contains a text input.
     * This method tries to estimate content size without actually rendering it,
     * which is a common approach in JavaFX for dynamic sizing.
     * For truly precise sizing, one would need to render a temporary Text node,
     * but this approximation is usually sufficient and avoids more complex dependencies.
     */
    private void autoSizeForContent(String message, boolean hasTextInput) {
        // Base dimensions for the dialog without much content
        double calculatedWidth = MIN_WIDTH;
        double calculatedHeight = MIN_HEIGHT;

        if (message != null && !message.isEmpty()) {
            int messageLength = message.length();

            // Estimate width: a longer message will try to expand horizontally up to MAX_WIDTH
            // The factor (e.g., 2.0) determines how aggressively it grows with length
            double lengthFactor = 2.0; // How much width per character after a certain threshold
            double baseMessageWidth = 300.0; // Base width for a "standard" message before it starts expanding

            if (messageLength > 30) { // Start expanding width after 30 characters
                calculatedWidth = Math.max(baseMessageWidth, MIN_WIDTH + (messageLength - 30) * lengthFactor);
            } else {
                calculatedWidth = MIN_WIDTH; // Use min width for very short messages
            }
            calculatedWidth = Math.min(MAX_WIDTH, calculatedWidth); // Cap at max width

            // Estimate height: more lines mean more height.
            // This is a rough estimation of characters per line given the calculated width.
            // Adjust the 50.0 value based on font size and target aesthetics.
            double charsPerLineEstimate = Math.max(30, (calculatedWidth - DIALOG_PADDING * 2) / 7.0); // Rough character width (7.0 for avg char width)
            int estimatedLines = (int) Math.ceil(messageLength / charsPerLineEstimate);
            estimatedLines = Math.max(1, estimatedLines); // Ensure at least one line height

            // Adjust base height with estimated lines, adding space per line
            // Adding a fixed amount per line for aesthetic spacing
            calculatedHeight = MIN_HEIGHT + (estimatedLines - 1) * 20;
        }

        // Add extra height if there's a text input field
        if (hasTextInput) {
            calculatedHeight += 65; // Extra space for TextField, its padding, and a bit more for layout
        }

        // Ensure calculated sizes are within min/max bounds before setting
        setPreferredSize(
                Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, calculatedWidth)),
                Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, calculatedHeight))
        );
    }

    private void setPreferredSize(double width, double height) {
        contentContainer.setPrefSize(width, height);
        // Explicitly set min/max size to ensure hard limits are enforced by the layout manager
        contentContainer.setMinSize(MIN_WIDTH, MIN_HEIGHT);
        contentContainer.setMaxSize(MAX_WIDTH, MAX_HEIGHT);
    }

    // This method can be used to set the modality if needed before calling showAndWait()
    // Currently, showAndWait() defaults to APPLICATION_MODAL unless explicitly overridden.
    public EnhancedAlert setModal(boolean modal) {
        // You could store this preference if you want a custom behavior for showAndWait().
        // For simplicity, current showAndWait() has a 'modal' parameter.
        return this;
    }

    // Show and close methods
    public Optional<AlertResult> showAndWait() {
        return showAndWait(true); // Default to application modal
    }

    public Optional<AlertResult> showAndWait(boolean modal) {
        popupStage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);

        // Reset animation state BEFORE showing, so it starts from the correct initial hidden position
        resetAnimationState();
        createEntryAnimation().play();

        popupStage.showAndWait();
        return Optional.ofNullable(result);
    }

    private void closeWithAnimation() {
        createExitAnimation().play();
    }

    // Animation methods
    private void resetAnimationState() {
        contentContainer.setOpacity(0);
        contentContainer.setScaleX(0.85); // Slightly smaller at start
        contentContainer.setScaleY(0.85);
        contentContainer.setTranslateY(15); // Slightly offset from center at start
    }

    private Timeline createEntryAnimation() {
        return new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(contentContainer.opacityProperty(), 0, Interpolator.EASE_OUT),
                        new KeyValue(contentContainer.scaleXProperty(), 0.85, Interpolator.EASE_OUT),
                        new KeyValue(contentContainer.scaleYProperty(), 0.85, Interpolator.EASE_OUT),
                        new KeyValue(contentContainer.translateYProperty(), 15, Interpolator.EASE_OUT)
                ),
                new KeyFrame(ANIMATION_DURATION,
                        new KeyValue(contentContainer.opacityProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(contentContainer.scaleXProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(contentContainer.scaleYProperty(), 1, Interpolator.EASE_OUT),
                        new KeyValue(contentContainer.translateYProperty(), 0, Interpolator.EASE_OUT)
                )
        );
    }

    private Timeline createExitAnimation() {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(contentContainer.opacityProperty(), 1, Interpolator.EASE_IN),
                        new KeyValue(contentContainer.scaleXProperty(), 1, Interpolator.EASE_IN),
                        new KeyValue(contentContainer.scaleYProperty(), 1, Interpolator.EASE_IN),
                        new KeyValue(contentContainer.translateYProperty(), 0, Interpolator.EASE_IN)
                ),
                new KeyFrame(FAST_ANIMATION,
                        new KeyValue(contentContainer.opacityProperty(), 0, Interpolator.EASE_IN),
                        new KeyValue(contentContainer.scaleXProperty(), 0.9, Interpolator.EASE_IN),
                        new KeyValue(contentContainer.scaleYProperty(), 0.9, Interpolator.EASE_IN),
                        new KeyValue(contentContainer.translateYProperty(), 10, Interpolator.EASE_IN) // Slight drop on exit
                )
        );
        timeline.setOnFinished(e -> {
            // Do NOT call resetAnimationState() here. It causes a flicker.
            // State is reset before next showAndWait().
            popupStage.close();
        });
        return timeline;
    }
}