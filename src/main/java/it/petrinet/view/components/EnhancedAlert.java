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
 * Fixed animation issues with consecutive alerts while maintaining original background behavior.
 */
public class EnhancedAlert {

    // Constants
    private static final String CSS_PATH = "/styles/style.css";
    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    private static final Duration FAST_ANIMATION = Duration.millis(200);

    // Sizing limits
    private static final double MIN_WIDTH = 320;
    private static final double MAX_WIDTH = 650;
    private static final double MIN_HEIGHT = 140;
    private static final double MAX_HEIGHT = 300;

    // Layout constants
    private static final double CONTENT_SPACING = 18;
    private static final double BUTTON_SPACING = 15;
    private static final double DIALOG_PADDING = 25;

    // Global animation state management
    private static Timeline globalExitAnimation;
    private static final Object animationLock = new Object();

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
    private Timeline currentAnimation;
    private boolean isClosing = false;

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
        contentContainer.setMaxWidth(Region.USE_PREF_SIZE);
        contentContainer.setMaxHeight(Region.USE_PREF_SIZE);

        // Configure dialog content
        dialogContent.setAlignment(Pos.CENTER);
        dialogContent.setFillWidth(true);

        // Add dialog content to container
        contentContainer.getChildren().add(dialogContent);

        // Prevent event bubbling
        contentContainer.setOnMouseClicked(Event::consume);

        // Add container to root
        popupRoot.getChildren().add(contentContainer);
        StackPane.setAlignment(contentContainer, Pos.CENTER);

        // Create and set scene
        Scene popupScene = new Scene(popupRoot, ownerStage.getWidth(), ownerStage.getHeight());
        popupScene.setFill(Color.TRANSPARENT);
        popupStage.setScene(popupScene);
    }

    private void setupStyling() {
        // Original background styling
        popupRoot.setStyle("-fx-background-color: rgb(30,30,46,0.75);");
        contentContainer.getStyleClass().add("enhanced-alert-pane");

        try {
            if (getClass().getResource(CSS_PATH) != null) {
                popupStage.getScene().getStylesheets().add(
                        Objects.requireNonNull(getClass().getResource(CSS_PATH)).toExternalForm()
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
                if (evt.getCode() == KeyCode.ESCAPE && !isClosing) {
                    result = new AlertResult(ButtonType.CANCEL, null);
                    closeWithAnimation();
                }
            });
        });

        // Mouse handlers - click outside to close
        popupRoot.setOnMouseClicked(evt -> {
            if (evt.getTarget() == popupRoot && !isClosing) {
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
        textField.setPrefWidth(280);

        Button okButton = createButton("OK", ButtonType.OK, true);
        Button cancelButton = createButton("Cancel", ButtonType.CANCEL, false);

        okButton.setOnAction(e -> {
            result = new AlertResult(ButtonType.OK, textField.getText());
            closeWithAnimation();
        });

        textField.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) okButton.fire();
            if (e.getCode() == KeyCode.ESCAPE) cancelButton.fire();
        });

        HBox buttonBox = new HBox(BUTTON_SPACING, okButton, cancelButton);
        buttonBox.setAlignment(Pos.CENTER);

        dialogContent.getChildren().setAll(titleLabel, messageLabel, textField, buttonBox);
        autoSizeForContent(message, true);

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
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
        return label;
    }

    private Label createMessageLabel(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("message-label");
        label.setWrapText(true);
        label.setAlignment(Pos.CENTER);
        label.setMaxWidth(Double.MAX_VALUE);
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

    private void autoSizeForContent(String message, boolean hasTextInput) {
        double calculatedWidth = MIN_WIDTH;
        double calculatedHeight = MIN_HEIGHT;

        if (message != null && !message.isEmpty()) {
            int messageLength = message.length();
            double lengthFactor = 2.0;
            double baseMessageWidth = 300.0;

            if (messageLength > 30) {
                calculatedWidth = Math.max(baseMessageWidth, MIN_WIDTH + (messageLength - 30) * lengthFactor);
            } else {
                calculatedWidth = MIN_WIDTH;
            }
            calculatedWidth = Math.min(MAX_WIDTH, calculatedWidth);

            double charsPerLineEstimate = Math.max(30, (calculatedWidth - DIALOG_PADDING * 2) / 7.0);
            int estimatedLines = (int) Math.ceil(messageLength / charsPerLineEstimate);
            estimatedLines = Math.max(1, estimatedLines);

            calculatedHeight = MIN_HEIGHT + (estimatedLines - 1) * 20;
        }

        if (hasTextInput) {
            calculatedHeight += 65;
        }

        setPreferredSize(
                Math.max(MIN_WIDTH, Math.min(MAX_WIDTH, calculatedWidth)),
                Math.max(MIN_HEIGHT, Math.min(MAX_HEIGHT, calculatedHeight))
        );
    }

    private void setPreferredSize(double width, double height) {
        contentContainer.setPrefSize(width, height);
        contentContainer.setMinSize(MIN_WIDTH, MIN_HEIGHT);
        contentContainer.setMaxSize(MAX_WIDTH, MAX_HEIGHT);
    }

    public EnhancedAlert setModal(boolean modal) {
        return this;
    }

    // Show and close methods
    public Optional<AlertResult> showAndWait() {
        return showAndWait(true);
    }

    public Optional<AlertResult> showAndWait(boolean modal) {
        popupStage.initModality(modal ? Modality.APPLICATION_MODAL : Modality.NONE);

        synchronized (animationLock) {
            // Wait for any global exit animation to complete
            if (globalExitAnimation != null && globalExitAnimation.getStatus() == Timeline.Status.RUNNING) {
                try {
                    // Wait briefly for the exit animation to complete
                    animationLock.wait(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }

        // Reset animation state and start entry animation
        resetAnimationState();
        createEntryAnimation().play();

        popupStage.showAndWait();
        return Optional.ofNullable(result);
    }

    private void closeWithAnimation() {
        if (isClosing) return;
        isClosing = true;

        // Stop any current animation
        if (currentAnimation != null) {
            currentAnimation.stop();
        }

        createExitAnimation().play();
    }

    // Animation methods
    private void resetAnimationState() {
        contentContainer.setOpacity(0);
        contentContainer.setScaleX(0.85);
        contentContainer.setScaleY(0.85);
        contentContainer.setTranslateY(15);
    }

    private Timeline createEntryAnimation() {
        currentAnimation = new Timeline(
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
        return currentAnimation;
    }

    private Timeline createExitAnimation() {
        currentAnimation = new Timeline(
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
                        new KeyValue(contentContainer.translateYProperty(), 10, Interpolator.EASE_IN)
                )
        );

        // Store as global exit animation for synchronization
        globalExitAnimation = currentAnimation;

        currentAnimation.setOnFinished(e -> {
            synchronized (animationLock) {
                animationLock.notifyAll();
            }
            popupStage.close();
        });

        return currentAnimation;
    }
}