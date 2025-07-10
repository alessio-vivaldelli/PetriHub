package it.petrinet.view.components;

import it.petrinet.utils.IconUtils;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.function.Consumer;

/**
 * Factory class for creating notification UI components in a JavaFX application.
 * <p>
 * Provides methods to generate styled notification items with message text,
 * colored icon, timestamp, and a hoverable close button that removes the
 * notification with a fade-out animation.
 * </p>
 */
public class NotificationFactory {

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
        /** A computation end event. */
        END_COMPUTATION,
    }

    /**
     * Creates a notification item given a numeric type index.
     * <p>
     * Delegates to {@link #createNotificationItem(MessageType, String, String, long)}
     * after validating the type index.
     * </p>
     *
     * @param type      the numeric index of the message type (0-based)
     * @param sender    the name of the entity triggering the notification
     * @param netName   the name of the Petri net associated with the event
     * @param timestamp the event timestamp in epoch milliseconds
     * @return a JavaFX Node representing the styled notification, or null if the type is invalid
     */
    public static Node createNotificationItem(int type, String sender, String netName, long timestamp) {
        if (type < 0 || type >= MessageType.values().length) {
            return null;
        }
        return createNotificationItem(MessageType.values()[type], sender, netName, timestamp);
    }

    /**
     * Creates a styled notification item for a specific {@link MessageType}.
     * <p>
     * Each notification consists of:
     * <ul>
     *   <li>A colored square icon indicating the type</li>
     *   <li>A message label</li>
     *   <li>A timestamp label</li>
     *   <li>A close button displayed on hover to remove the item</li>
     * </ul>
     * </p>
     *
     * @param type      the message type enum
     * @param sender    the name of the entity triggering the notification
     * @param netName   the name of the Petri net associated with the event
     * @param timestamp the event timestamp in epoch milliseconds
     * @return a JavaFX Node ready to be added to a container
     */
    public static Node createNotificationItem(MessageType type, String sender, String netName, long timestamp) {
        String message = getMessageText(type, sender, netName);
        String colorHex = getColorByType(type);

        HBox item = new HBox(12);
        item.setPadding(new Insets(12));
        item.setStyle("-fx-background-color: #45475a; -fx-background-radius: 8;");

        // Colored icon region
        Region colorRegion = new Region();
        colorRegion.setPrefSize(32, 32);
        colorRegion.setBackground(new Background(
                new BackgroundFill(Color.web(colorHex), new CornerRadii(8), Insets.EMPTY)
        ));

        // Message label
        Label messageLabel = new Label(message);
        messageLabel.setStyle("-fx-text-fill: #cdd6f4; -fx-font-size: 14; -fx-font-weight: 500;");

        // Timestamp label
        Label timestampLabel = new Label(formatTimestamp(timestamp));
        timestampLabel.setStyle("-fx-text-fill: #bac2de; -fx-font-size: 12;");

        VBox textBox = new VBox(2, messageLabel, timestampLabel);
        HBox.setHgrow(textBox, Priority.ALWAYS);

        // Close button (hidden by default)
        Button closeBtn = new Button();
        IconUtils.setIcon(closeBtn,"close", 20);
        closeBtn.setVisible(false);
        closeBtn.getStyleClass().add("notification-close-btn");

        // Show/hide close button on hover
        item.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> closeBtn.setVisible(true));
        item.addEventHandler(MouseEvent.MOUSE_EXITED, e -> closeBtn.setVisible(false));

        // Remove item with fade-out animation when close button is clicked
        closeBtn.setOnMouseClicked(e -> animateRemoval(item));

        item.getChildren().addAll(colorRegion, textBox, closeBtn);
        return item;
    }

    public static HBox noNotificationsPlaceholder() {
        Label emptyLabel = new Label("No notifications available");
        emptyLabel.setStyle(
                "-fx-text-fill: #bac2de; " +
                        "-fx-font-size: 14; " +
                        "-fx-font-style: italic;"
        );
        HBox placeholderBox = new HBox(emptyLabel);
        placeholderBox.setAlignment(Pos.CENTER);
        placeholderBox.setPadding(new Insets(24));
        return placeholderBox;
    }

    public static void setOnMouseClicker(Consumer<Node> onMouseClicked) {

    }

    /**
     * Animates the removal of the notification HBox with a fade-out effect,
     * then adds a centered "No notifications" placeholder if the container
     * becomes empty.
     *
     * @param item the HBox to remove from its parent
     */
    private static void animateRemoval(HBox item) {
        Pane parent = (Pane) item.getParent();
        FadeTransition ft = new FadeTransition(Duration.millis(200), item);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setOnFinished(evt -> {
            if (parent != null) {
                parent.getChildren().remove(item);

                // Add placeholder if container is empty
                if (parent.getChildren().isEmpty()) {
                    Label emptyLabel = new Label("No notifications");
                    emptyLabel.setStyle(
                            "-fx-text-fill: #bac2de; " +
                                    "-fx-font-size: 14; " +
                                    "-fx-font-style: italic;"
                    );
                    HBox placeholderBox = new HBox(emptyLabel);
                    placeholderBox.setAlignment(Pos.CENTER);
                    placeholderBox.setPadding(new Insets(24));
                    if (parent instanceof VBox) {
                        ((VBox) parent).setAlignment(Pos.CENTER);
                    }
                    parent.getChildren().add(placeholderBox);
                }
            }
        });
        ft.play();
    }

    /**
     * Returns the display text for a given notification type.
     *
     * @param type    the message type
     * @param sender  the name of the sender
     * @param netName the name of the net involved
     * @return a formatted message string
     */
    private static String getMessageText(MessageType type, String sender, String netName) {
        return switch (type) {
            case SUBSCRIPTION       -> sender + " subscribed to " + netName;
            case STARTED_COMPUTATION-> sender + " has started the computation on " + netName;
            case FIRED_TRANSITION   -> sender + " has fired a transition on " + netName;
            case END_COMPUTATION    -> sender + "'s computation on " + netName + " has ended";
        };
    }

    /**
     * Returns the hex color code for a given message type.
     *
     * @param type the message type
     * @return a hex color string
     */
    private static String getColorByType(MessageType type) {
        return switch (type) {
            case SUBSCRIPTION       -> "#89b4fa";
            case STARTED_COMPUTATION-> "#a6e3a1";
            case FIRED_TRANSITION   -> "#f9e2af";
            case END_COMPUTATION    -> "#f38ba8";
        };
    }

    /**
     * Formats a timestamp (epoch millis) into a human-readable string.
     *
     * @param timestamp epoch milliseconds
     * @return formatted date/time string in pattern "dd/MM/yy HH:mm"
     */
    private static String formatTimestamp(long timestamp) {
        var formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yy HH:mm");
        return java.time.Instant.ofEpochMilli(timestamp)
                .atZone(java.time.ZoneId.systemDefault())
                .format(formatter);
    }
}
