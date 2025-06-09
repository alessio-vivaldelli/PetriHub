package it.petrinet.controller;

import it.petrinet.model.NetCategory;
import it.petrinet.model.PetriNetRow;
import it.petrinet.model.PetriNetRow.Status;
import it.petrinet.model.User;
import it.petrinet.utils.BadgeCell;
import it.petrinet.utils.BadgeCell.Category;
import it.petrinet.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomeController {

    //Quick access buttons
    @FXML private VBox activityFeedContainer;
    @FXML private Label analysesPerformedLabel;
    @FXML private Label simulationsRunLabel;
    @FXML private Label totalNetsLabel;

    //Recent nets section
    @FXML private TableView<PetriNetRow> tableView;
    @FXML private TableColumn<PetriNetRow, String> nameCol;
    @FXML private TableColumn<PetriNetRow, String> authorCol;
    @FXML private TableColumn<PetriNetRow, LocalDateTime> dateCol;
    @FXML private TableColumn<PetriNetRow, Status> statusCol;
    @FXML private TableColumn<PetriNetRow, NetCategory> typeCol;

    //user info
    @FXML private Label userNameLabel;

    @FXML
    private void initialize() {
        // User init
        User user = ViewNavigator.getAuthenticatedUser();
        userNameLabel.setText(user != null ? "Welcome, " + user.getUsername() : "Welcome, Guest!");

        //Matrix init
        nameCol.setCellValueFactory(c -> c.getValue().nameProperty());
        authorCol.setCellValueFactory(c -> c.getValue().authorProperty());

        dateCol.setCellFactory(col -> new TableCell<>() {
            private final DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            @Override
            protected void updateItem(LocalDateTime dt, boolean empty) {
                super.updateItem(dt, empty);
                setText(empty || dt == null ? null : dt.format(fmt));
            }
        });

        // BadgeCell per Stato e Tipo
        statusCol.setCellFactory(col -> new BadgeCell<>(Category.STATUS));
        typeCol.setCellFactory(col -> new BadgeCell<>(Category.TYPE));

        //onClick per le righe della tabella
        tableView.setRowFactory(tv -> {
            TableRow<PetriNetRow> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty())) {
                    // Single click handler
                    PetriNetRow selectedItem = row.getItem();
                    handleRowClick(selectedItem);
                }
            });
            return row;
        });

        //TODO: impostare la logica di caricamento dei dati
        populateTable();


    }

    private void handleRowClick(PetriNetRow selectedItem) {
        ViewNavigator.navigateToMyNets();
    }

    private void populateTable() {
        // Simulazione di dati recenti
        tableView.getItems().addAll(
            PetriNetRow.of("Net 1", "Alice", LocalDateTime.now().minusDays(1), Status.COMPLETED, NetCategory.SUBSCRIBED),
            PetriNetRow.of("Net 2", "Bob", LocalDateTime.now().minusDays(2), Status.IN_PROGRESS, NetCategory.DISCOVER),
            PetriNetRow.of("Net 3", "Charlie", LocalDateTime.now().minusDays(3), Status.STARTED, NetCategory.SUBSCRIBED),
            PetriNetRow.of("Net 4", "David", LocalDateTime.now().minusDays(4), Status.WAITING, NetCategory.DISCOVER)

        );
    }

    public void handleNewNet(ActionEvent event) {
    }
}