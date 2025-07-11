package it.petrinet.view.components;

import it.petrinet.utils.IconUtils;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Factory class for creating notification UI components in a JavaFX application
 * using a fluent Builder pattern.
 * <p>
 * Provides methods to generate styled notification items with a message, icon,
 * timestamp, and optional actions for clicking and closing.
 * </p>
 */
public final class NotificationFactory {

    private NotificationFactory() {
        // Private constructor to prevent instantiation
    }

    /**
     * Types of notifications supported by the factory.
     */
    public enum MessageType {
        /** A user subscription event. */
        SUBSCRIPTION,
        /** A computation start event. */
        STARTED_COMPUTATION,
        /** A transition fired event. */
        FIRED_TRANSITION,
        /** A computation restart event. */
        RESTART,
        /** A computation end event. */
        END_COMPUTATION,
    }

    /**
     * Creates a new instance of the NotificationBuilder.
     *
     * @return A new {@link NotificationBuilder} instance.
     */
    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    /**
     * The builder class for constructing a notification Node.
     */
    public static class NotificationBuilder {
        private MessageType type;
        private String sender;
        private String netName;
        private long timestamp;
        private Runnable onCancelItem;
        private Runnable onItemClick;

        private NotificationBuilder() {
            this.timestamp = System.currentTimeMillis()/1000;
        }

        public NotificationBuilder withType(MessageType type) {
            this.type = type;
            return this;
        }

        public NotificationBuilder withSender(String sender) {
            this.sender = sender;
            return this;
        }

        public NotificationBuilder withNetName(String netName) {
            this.netName = netName;
            return this;
        }

        public NotificationBuilder withTimestamp(long timestamp) {
            this.timestamp = timestamp;
            return this;
        }

        public NotificationBuilder onCancelItem(Runnable onCancelItem) {
            this.onCancelItem = onCancelItem;
            return this;
        }

        public NotificationBuilder onItemClick(Runnable onItemClick) {
            this.onItemClick = onItemClick;
            return this;
        }

        /**
         * Constructs the notification Node with the specified properties.
         *
         * @return A JavaFX Node representing the styled notification.
         * @throws IllegalStateException if required fields (type, sender, netName) are not set.
         */
        public Node build() {
            Objects.requireNonNull(type, "MessageType cannot be null.");
            Objects.requireNonNull(sender, "Sender cannot be null.");
            Objects.requireNonNull(netName, "Net name cannot be null.");

            String message = getMessageText(type, sender, netName);
            String colorHex = getColorByType(type);

            HBox item = new HBox(12);
            item.setPadding(new Insets(12));
            Background baseBackground = new Background(new BackgroundFill(Color.web("#45475a"), new CornerRadii(8), Insets.EMPTY));
            item.setBackground(baseBackground);

            Region colorRegion = new Region();
            colorRegion.setPrefSize(32, 32);
            colorRegion.setBackground(new Background(new BackgroundFill(Color.web(colorHex), new CornerRadii(8), Insets.EMPTY)));

            Label messageLabel = new Label(message);
            messageLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 14; -fx-font-weight: 500;");

            Label timestampLabel = new Label(formatTimestamp(timestamp));
            timestampLabel.setStyle("-fx-text-fill: #bac2de; -fx-font-size: 12;");

            VBox textBox = new VBox(2, messageLabel, timestampLabel);
            HBox.setHgrow(textBox, Priority.ALWAYS);

            Button closeBtn = new Button();
            IconUtils.setIcon(closeBtn, "close", 20);
            closeBtn.setVisible(false);
            closeBtn.getStyleClass().add("notification-close-btn");

            closeBtn.setOnMouseClicked(e -> {
                animateRemoval(item, onCancelItem);
                e.consume();
            });

            if (onItemClick != null) {
                item.setCursor(Cursor.HAND);
                Background hoverBackground = new Background(new BackgroundFill(Color.web("#585b70"), new CornerRadii(8), Insets.EMPTY));
                item.setOnMouseClicked(e -> onItemClick.run());

                item.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                    item.setBackground(hoverBackground);
                    closeBtn.setVisible(true);
                });
                item.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                    item.setBackground(baseBackground);
                    closeBtn.setVisible(false);
                });
            } else {
                item.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> closeBtn.setVisible(true));
                item.addEventHandler(MouseEvent.MOUSE_EXITED, e -> closeBtn.setVisible(false));
            }

            item.getChildren().addAll(colorRegion, textBox, closeBtn);
            return item;
        }
    }

    /**
     * Creates a placeholder to show when no notifications are available.
     *
     * @return A styled HBox containing the "No notifications available" message.
     */
    public static HBox noNotificationsPlaceholder() {
        Label emptyLabel = new Label("No notifications available");
        emptyLabel.setStyle("-fx-text-fill: #6c7086; -fx-font-size: 14; -fx-font-style: italic;");
        HBox placeholderBox = new HBox(emptyLabel);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setPadding(new Insets(24));
        placeholderBox.setMaxWidth(Double.MAX_VALUE);
        placeholderBox.setPrefHeight(200);
        placeholderBox.setId("no-notifications-placeholder");
        return placeholderBox;
    }

    /**
     * Animates the removal of the notification, then updates the container.
     *
     * @param item         The HBox to remove.
     * @param onCancelItem The callback to run after removal.
     */
    private static void animateRemoval(HBox item, Runnable onCancelItem) {
        if (item.getParent() == null) return;
        Pane parent = (Pane) item.getParent();
        FadeTransition ft = new FadeTransition(Duration.millis(200), item);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(evt -> {
            parent.getChildren().remove(item);
            if (onCancelItem != null) {
                onCancelItem.run();
            }
        });
        ft.play();
    }

    private static String getMessageText(MessageType type, String sender, String netName) {
        return switch (type) {
            case SUBSCRIPTION -> String.format("%s subscribed to %s", sender, netName);
            case STARTED_COMPUTATION -> String.format("%s has started the computation on %s", sender, netName);
            case FIRED_TRANSITION -> String.format("%s has fired a transition on %s", sender, netName);
            case RESTART -> String.format("%s has restarted the computation on %s", sender, netName);
            case END_COMPUTATION -> String.format("%s's computation on %s has ended", sender, netName);
        };
    }

    private static String getColorByType(MessageType type) {
        return switch (type) {
            case SUBSCRIPTION -> "#89b4fa";
            case STARTED_COMPUTATION -> "#a6e3a1";
            case FIRED_TRANSITION -> "#f9e2af";
            case RESTART -> "#fab387";
            case END_COMPUTATION -> "#f38ba8";
        };
    }

    private static String formatTimestamp(long timestamp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        return Instant.ofEpochMilli(timestamp)
                .atZone(ZoneId.systemDefault())
                .format(formatter);
    }
}
