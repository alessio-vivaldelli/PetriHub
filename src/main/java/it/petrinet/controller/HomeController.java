package it.petrinet.controller;

import it.petrinet.view.components.NetCategory;
import it.petrinet.view.components.TableElement;
import it.petrinet.model.User;
import it.petrinet.view.ViewNavigator;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;

import java.time.format.DateTimeFormatter;

public class HomeController {

    public VBox activityFeedContainer;
    public Label analysesPerformedLabel;
    public Label simulationsRunLabel;
    public Label totalNetsLabel;

    @FXML private Label userNameLabel;
    @FXML private ListView<TableElement> netsListView;

    private final DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @FXML
    private void initialize() {
        // User
        User user = ViewNavigator.getAuthenticatedUser();
        userNameLabel.setText(user != null ? "Welcome, " + user.getUsername() : "Welcome, Guest!");

        // Custom Cell Factory
        netsListView.setCellFactory(lv -> new ListCell<>() {
            private final HBox container = new HBox(10);
            private final Label typeLabel = new Label();
            private final Label nameLabel = new Label();
            private final Label authorLabel = new Label();
            private final Label dateLabel = new Label();
            private final Label statusLabel = new Label();

            {
                // -- RESPONSIVE LAYOUT --
                // Allow the nameLabel to grow and fill available space
                HBox.setHgrow(nameLabel, Priority.ALWAYS);

                // Set fixed minimum widths for other columns so they don't shrink too much
                typeLabel.setMinWidth(100);
                authorLabel.setMinWidth(120);
                dateLabel.setMinWidth(100);
                statusLabel.setMinWidth(100);

                // Center align text where appropriate
                typeLabel.setAlignment(Pos.CENTER);
                statusLabel.setAlignment(Pos.CENTER);
                dateLabel.setAlignment(Pos.CENTER_RIGHT);

                container.setAlignment(Pos.CENTER_LEFT);
                container.setPadding(new Insets(4, 10, 4, 10)); // Add some vertical padding

                container.getChildren().addAll(typeLabel, nameLabel, authorLabel, dateLabel, statusLabel);

                // -- INTERACTIONS --
                this.setOnMouseClicked(e -> {
                    if (!isEmpty() && e.getButton() == MouseButton.PRIMARY && e.getClickCount() == 1) {
                        ViewNavigator.navigateToDetail(getItem());
                    }
                });

                // Subtle hover effect
                setOnMouseEntered(e -> setStyle("-fx-background-color: #45475a; -fx-background-radius: 4;"));
                setOnMouseExited(e -> setStyle(""));
            }

            @Override
            protected void updateItem(TableElement net, boolean empty) {
                super.updateItem(net, empty);
                if (empty || net == null) {
                    setGraphic(null);
                } else {
                    // Populate data
                    nameLabel.setText(net.getName());
                    authorLabel.setText(net.getAuthor());
                    dateLabel.setText(net.getLastModified().format(dateFmt));

                    // Set status badge style
                    switch (net.getStatus()) {
                        case completed -> setBadge(statusLabel, "Completed", "#a6e3a1"); // Green
                        case notStarted -> setBadge(statusLabel, "Not Started", "#f38ba8"); // Red
                        case waiting -> setBadge(statusLabel, "Waiting", "#fab387"); // Peach
                        case inProgress -> setBadge(statusLabel, "In Progress", "#89b4fa"); // Blue
                    }

                    // Set type badge style
                    switch (net.getType()) {
                        case myNets -> setBadge(typeLabel, "Owned", "#cba6f7"); // Mauve
                        case mySubs -> setBadge(typeLabel, "Subscribed", "#74c7ec"); // Sapphire
                        case discover -> setBadge(typeLabel, "Discover", "#94e2d5"); // Teal
                    }

                    setGraphic(container);
                }
            }

            // Helper for creating minimalist badges
            private void setBadge(Label lbl, String text, String bgColor) {
                lbl.setText(text);
                lbl.setAlignment(Pos.CENTER);
                lbl.getStyleClass().add("badge");
                // Style with Catppuccin Mocha colors
                lbl.setStyle(String.format(
                        "-fx-background-color: %s; " +
                                "-fx-text-fill: #1e1e2e; " + // Base color for text for high contrast
                                "-fx-background-radius: 12; " + // Pill shape
                                "-fx-font-size: 11px; " +
                                "-fx-font-weight: bold; " +
                                "-fx-padding: 4px 12px;",
                        bgColor
                ));
            }
        });

        // Example Data (can be replaced with DB/service call)
        netsListView.setItems(FXCollections.observableArrayList(
                new TableElement("Flow A", "Alice", java.time.LocalDate.now().minusDays(2), TableElement.Status.inProgress, NetCategory.myNets),
                new TableElement("Flow B", "Bob", java.time.LocalDate.now().minusDays(5), TableElement.Status.completed, NetCategory.mySubs),
                new TableElement("Flow C", "Charlie", java.time.LocalDate.now().minusDays(1), TableElement.Status.waiting, NetCategory.discover),
                new TableElement("Flow D", "Alice", java.time.LocalDate.now().minusDays(3), TableElement.Status.notStarted, NetCategory.myNets)
        ));

        // To restrain the ListView in a certain space, you can set its max width/height in the FXML
        // For example: <ListView fx:id="netsListView" maxWidth="800" ... />
        netsListView.setMaxHeight(300); // Set a max height to prevent it from growing too large
        netsListView.setPlaceholder(new Label("No nets available. Create or subscribe to some!"));
    }


    public void handleNewNet(ActionEvent event) {
    }
}