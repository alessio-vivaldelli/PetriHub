package it.petrinet.view.components;

import javafx.animation.*;
import javafx.beans.value.ChangeListener;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.util.Optional;

/**
 * Enhanced PopupMenu with modal behavior and simulated blur (dimming effect).
 * Uses ChangeListeners for stage position and size synchronization.
 * This version simplifies stage visibility for showAndWait compatibility.
 */
public class PopupMenu { // test

    private final Stage ownerStage;
    private final StackPane root;

    private Stage popupStage;
    private StackPane popupRoot;
    private StackPane contentContainer;

    private String result;

    private static final Duration ANIMATION_DURATION = Duration.millis(300);
    private static final double OVERLAY_DIMMING_OPACITY = 0.6;

    private ChangeListener<Number> widthListener;
    private ChangeListener<Number> heightListener;
    private ChangeListener<Number> xListener;
    private ChangeListener<Number> yListener;

    public PopupMenu(Stage ownerStage) {
        this.ownerStage = ownerStage;

        Node sceneRoot = ownerStage.getScene().getRoot();
        if (sceneRoot instanceof StackPane) {
            root = (StackPane) sceneRoot;
        } else {
            root = new StackPane(sceneRoot);
            ownerStage.getScene().setRoot(root);
        }

        popupStage = new Stage();
        popupStage.initOwner(ownerStage);
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.initStyle(StageStyle.TRANSPARENT);

        popupRoot = new StackPane();
        popupRoot.setStyle("-fx-background-color: rgba(0,0,0,0.0);"); // Initial transparent background

        contentContainer = new StackPane();
        contentContainer.setStyle(
                "-fx-background-color: #1e1e2e; " +
                        "-fx-background-radius: 12; " +
                        "-fx-padding: 20; " +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 15, 0.3, 0, 6);"
        );
        contentContainer.setMaxWidth(500);
        contentContainer.setMaxHeight(300);
        contentContainer.setOnMouseClicked(e -> e.consume());

        contentContainer.setOpacity(0);
        contentContainer.setScaleX(0.8);
        contentContainer.setScaleY(0.8);

        popupRoot.getChildren().add(contentContainer);

        Scene popupScene = new Scene(popupRoot, ownerStage.getWidth(), ownerStage.getHeight());
        popupScene.setFill(Color.TRANSPARENT);
        popupStage.setScene(popupScene);

        widthListener = (obs, oldVal, newVal) -> popupStage.setWidth(newVal.doubleValue());
        heightListener = (obs, oldVal, newVal) -> popupStage.setHeight(newVal.doubleValue());
        xListener = (obs, oldVal, newVal) -> popupStage.setX(newVal.doubleValue());
        yListener = (obs, oldVal, newVal) -> popupStage.setY(newVal.doubleValue());

        popupStage.getScene().setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.ESCAPE) {
                result = null;
                popupStage.close();
            }
        });

        popupRoot.setOnMouseClicked(evt -> {
            if (evt.getTarget() == popupRoot) {
                result = null;
                popupStage.close();
            }
        });

        popupStage.setOnHidden(event -> {
            ownerStage.widthProperty().removeListener(widthListener);
            ownerStage.heightProperty().removeListener(heightListener);
            ownerStage.xProperty().removeListener(xListener);
            ownerStage.yProperty().removeListener(yListener);
        });
    }

    public void setContent(Node content) {
        contentContainer.getChildren().setAll(content);
    }

    public Optional<String> showAndWait() {
        result = null;

        popupStage.setWidth(ownerStage.getWidth());
        popupStage.setHeight(ownerStage.getHeight());
        popupStage.setX(ownerStage.getX());
        popupStage.setY(ownerStage.getY());

        ownerStage.widthProperty().addListener(widthListener);
        ownerStage.heightProperty().addListener(heightListener);
        ownerStage.xProperty().addListener(xListener);
        ownerStage.yProperty().addListener(yListener);

        // --- Prepare initial states for animations ---
        // Do NOT set popupStage.setOpacity(0) here if you want it to appear instantly by showAndWait().
        // Instead, let the overlay and content handle the visual "fade-in" from scratch.
        // If you absolutely need the stage itself to fade in, it requires a different approach
        // that delays the showAndWait() call, which complicates the return value.

        contentContainer.setOpacity(0);
        contentContainer.setScaleX(0.8);
        contentContainer.setScaleY(0.8);

        // --- Entry Animations ---

        // Animate the dimming overlay's opacity (popupRoot background)
        FadeTransition dimmingFadeIn = new FadeTransition(ANIMATION_DURATION, popupRoot);
        dimmingFadeIn.setFromValue(0);
        popupRoot.setStyle("-fx-background-color: rgba(0,0,0," + OVERLAY_DIMMING_OPACITY + ");");
        dimmingFadeIn.setToValue(OVERLAY_DIMMING_OPACITY);

        // Animate content scaling and fading
        ScaleTransition contentScaleIn = new ScaleTransition(ANIMATION_DURATION, contentContainer);
        contentScaleIn.setFromX(0.8);
        contentScaleIn.setFromY(0.8);
        contentScaleIn.setToX(1);
        contentScaleIn.setToY(1);
        contentScaleIn.setInterpolator(Interpolator.EASE_OUT);

        FadeTransition contentFadeIn = new FadeTransition(ANIMATION_DURATION, contentContainer);
        contentFadeIn.setFromValue(0);
        contentFadeIn.setToValue(1);

        // Combine only the content and dimming overlay animations
        ParallelTransition showTransition = new ParallelTransition(
                dimmingFadeIn,
                contentScaleIn,
                contentFadeIn
                // REMOVED: stageFadeInTimeline
        );

        // Play the animation *before* showAndWait().
        // showAndWait() will make the stage visible, and the animations will start from there.
        // This is the most common and compatible pattern to avoid "Stage already visible".
        showTransition.play();
        popupStage.showAndWait();

        // --- Exit Animations (run AFTER popupStage.showAndWait() returns) ---
        popupRoot.setStyle("-fx-background-color: rgba(0,0,0," + OVERLAY_DIMMING_OPACITY + ");");

        FadeTransition dimmingFadeOut = new FadeTransition(ANIMATION_DURATION, popupRoot);
        dimmingFadeOut.setFromValue(OVERLAY_DIMMING_OPACITY);
        dimmingFadeOut.setToValue(0);

        ScaleTransition contentScaleOut = new ScaleTransition(ANIMATION_DURATION, contentContainer);
        contentScaleOut.setFromX(1);
        contentScaleOut.setFromY(1);
        contentScaleOut.setToX(0.8);
        contentScaleOut.setToY(0.8);
        contentScaleOut.setInterpolator(Interpolator.EASE_IN);

        FadeTransition contentFadeOut = new FadeTransition(ANIMATION_DURATION, contentContainer);
        contentFadeOut.setFromValue(1);
        contentFadeOut.setToValue(0);

        // Combine exit animations
        ParallelTransition hideTransition = new ParallelTransition(
                dimmingFadeOut,
                contentScaleOut,
                contentFadeOut
                // REMOVED: stageFadeOutTimeline
        );

        hideTransition.setOnFinished(hEvt -> {
            popupRoot.setStyle("-fx-background-color: transparent;");
            contentContainer.setOpacity(0);
            contentContainer.setScaleX(0.8);
            contentContainer.setScaleY(0.8);
        });
        hideTransition.play();

        return Optional.ofNullable(result);
    }

    public void setResult(String res) {
        this.result = res;
        popupStage.close();
    }

    public boolean isShowing() {
        return popupStage.isShowing();
    }

    public void setContentStyle(String style) {
        contentContainer.setStyle(style);
    }

    public void setOverlayStyle(String style) {
    }
}