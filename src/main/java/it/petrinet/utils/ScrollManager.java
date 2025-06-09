package it.petrinet.utils;

import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.util.Duration;

import java.util.WeakHashMap;

public final class ScrollManager {
    private static final double SCROLL_EPSILON = 0.005;
    private static final double SCROLL_AMOUNT_PER_WHEEL_CLICK_PERCENT = 0.15;
    private static final double SCROLL_AMOUNT_TOUCHPAD_MULTIPLIER = 0.05;
    private static final Duration SCROLL_ANIMATION_DURATION = Duration.millis(250);
    private static final Interpolator SCROLL_INTERPOLATOR = Interpolator.EASE_OUT;
    private static final double DIRECTION_THRESHOLD = 0.7; // 70% dominant direction

    private static final WeakHashMap<ScrollPane, Timeline> scrollTimelines = new WeakHashMap<>();

    private ScrollManager() {}

    public static void setup(ScrollPane scrollPane, Rectangle leftShadow, Rectangle rightShadow) {
        if (scrollPane == null || leftShadow == null || rightShadow == null) {
            System.err.println("WARNING: Invalid parameters provided to setup method.");
            return;
        }

        // Initialize shadows as invisible
        leftShadow.setVisible(false);
        rightShadow.setVisible(false);
        leftShadow.setManaged(false);
        rightShadow.setManaged(false);

        // Setup shadow updater
        Runnable updateShadows = () -> {
            if (!(scrollPane.getContent() instanceof HBox)) {
                leftShadow.setVisible(false);
                leftShadow.setManaged(false);
                rightShadow.setVisible(false);
                rightShadow.setManaged(false);
                return;
            }

            HBox content = (HBox) scrollPane.getContent();
            double contentWidth = content.getLayoutBounds().getWidth();
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double hvalue = scrollPane.getHvalue();

            boolean isScrollable = contentWidth > viewportWidth + SCROLL_EPSILON;
            boolean showLeft = isScrollable && hvalue > SCROLL_EPSILON;
            boolean showRight = isScrollable && (1.0 - hvalue) > SCROLL_EPSILON;

            leftShadow.setVisible(showLeft);
            leftShadow.setManaged(showLeft);
            rightShadow.setVisible(showRight);
            rightShadow.setManaged(showRight);
        };

        // Property listeners
        scrollPane.hvalueProperty().addListener((obs, old, current) -> updateShadows.run());
        scrollPane.viewportBoundsProperty().addListener((obs, old, current) -> updateShadows.run());

        // Content listener
        scrollPane.contentProperty().addListener((obs, oldContent, newContent) -> {
            if (newContent != null) {
                newContent.layoutBoundsProperty().addListener((o, oldBounds, newBounds) -> {
                    Platform.runLater(updateShadows);
                });
            }
            Platform.runLater(updateShadows);
        });

        // Initial update
        Platform.runLater(updateShadows);

        // Configure smooth scrolling
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        scrollPane.addEventFilter(ScrollEvent.SCROLL, event -> {
            if (!(scrollPane.getContent() instanceof HBox)) return;

            HBox content = (HBox) scrollPane.getContent();
            double contentWidth = content.getLayoutBounds().getWidth();
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double scrollableWidth = contentWidth - viewportWidth;

            // Skip if not scrollable
            if (scrollableWidth <= SCROLL_EPSILON) return;

            // Calculate dominant scroll direction
            double absDeltaX = Math.abs(event.getDeltaX());
            double absDeltaY = Math.abs(event.getDeltaY());
            double totalDelta = absDeltaX + absDeltaY;

            if (totalDelta == 0) return; // No movement

            double xRatio = absDeltaX / totalDelta;
            double yRatio = absDeltaY / totalDelta;

            // Only handle horizontal scroll events
            if (xRatio < DIRECTION_THRESHOLD && yRatio > (1 - DIRECTION_THRESHOLD)) {
                return; // Vertical scroll - let it propagate
            }

            // Handle horizontal scroll
            event.consume();

            double currentHValue = scrollPane.getHvalue();
            double targetHValue;

            // Touchpad detection (both deltas present)
            boolean isTouchpad = absDeltaX > 0 && absDeltaY > 0;

            if (isTouchpad) {
                // Touchpad scroll - use both axes with sensitivity adjustment
                double touchpadDelta = (-event.getDeltaX() + event.getDeltaY()) * SCROLL_AMOUNT_TOUCHPAD_MULTIPLIER;
                targetHValue = currentHValue + touchpadDelta;
            } else if (event.getDeltaX() != 0) {
                // Direct horizontal scroll
                targetHValue = currentHValue - (event.getDeltaX() / scrollableWidth);
            } else {
                // Mouse wheel vertical scroll mapped to horizontal
                double direction = event.getDeltaY() > 0 ? -1 : 1;
                targetHValue = currentHValue + direction * SCROLL_AMOUNT_PER_WHEEL_CLICK_PERCENT;
            }

            // Clamp value
            targetHValue = Math.max(0.0, Math.min(1.0, targetHValue));

            // Animate scrolling
            Timeline timeline = scrollTimelines.computeIfAbsent(scrollPane, k -> new Timeline());
            timeline.stop();
            timeline.getKeyFrames().setAll(
                    new KeyFrame(SCROLL_ANIMATION_DURATION,
                            new KeyValue(scrollPane.hvalueProperty(), targetHValue, SCROLL_INTERPOLATOR)
                    )
            );
            timeline.play();
        });
    }
}