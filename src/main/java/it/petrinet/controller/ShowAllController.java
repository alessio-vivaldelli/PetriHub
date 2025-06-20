// src/main/java/it/petrinet/view/ShowAllController.java
package it.petrinet.controller;

import it.petrinet.model.PetriNetRow;
import it.petrinet.model.NetCategory;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.components.PetriNetTableComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class ShowAllController {

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private PetriNetTableComponent petriNetTable;
    private static NetCategory cardType;

    public static void setType(NetCategory type) {
        cardType = type;
    }

    @FXML
    public void initialize() {
        frameTitle.setText(" "+cardType.getDisplayName());
        IconUtils.setIcon(frameTitle, cardType.getDisplayName() + ".png", 30, 30 , Color.WHITE);

        // Initialize the table component and load data
        initializeTableComponent();
        loadShowAllData();
    }

    private void initializeTableComponent() {
        petriNetTable = new PetriNetTableComponent();

        // Set up row click handler
        petriNetTable.setOnRowClickHandler(this::handleTableRowClick);

        // Add the table component to the container
        tableContainer.getChildren().add(petriNetTable);
    }

    private void handleTableRowClick(PetriNetRow petriNetRow) {
        System.out.println("Row clicked: " + petriNetRow.nameProperty());
    }

    private void loadShowAllData() {
        List<PetriNetRow> allNets = switch (cardType) {
            case OWNED -> OwnSet();
            case SUBSCRIBED -> SubSet();
            case DISCOVER -> DisSet();
            default -> null;
        };
        petriNetTable.setData(allNets);
    }

    //TODO
    private List<PetriNetRow> DisSet() {
        return Arrays.asList(

                new PetriNetRow("Discover Net 1", "Alice", LocalDateTime.now(), PetriNetRow.Status.STARTED, NetCategory.DISCOVER),
                new PetriNetRow("Discover Net 2", "Bob", LocalDateTime.now().minusDays(1), PetriNetRow.Status.STARTED, NetCategory.DISCOVER),
                new PetriNetRow("Discover Net 3", "Charlie", LocalDateTime.now().minusDays(2), PetriNetRow.Status.STARTED, NetCategory.DISCOVER),
                new PetriNetRow("Discover Net 4", "David", LocalDateTime.now().minusDays(3), PetriNetRow.Status.STARTED, NetCategory.DISCOVER),
                new PetriNetRow("Discover Net 5", "Eve", LocalDateTime.now().minusDays(4), PetriNetRow.Status.STARTED, NetCategory.DISCOVER)
        );
    }

    //TODO
    private List<PetriNetRow> SubSet() {
        return Arrays.asList(
                new PetriNetRow("Subscribed Net 1", "Alice", LocalDateTime.now(), PetriNetRow.Status.WAITING, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 2", "Bob", LocalDateTime.now().minusDays(1), PetriNetRow.Status.COMPLETED, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 3", "Charlie", LocalDateTime.now().minusDays(2), PetriNetRow.Status.STARTED, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 4", "David", LocalDateTime.now().minusDays(3), PetriNetRow.Status.STARTED, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 5", "Eve", LocalDateTime.now().minusDays(4), PetriNetRow.Status.IN_PROGRESS, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 6", "Frank", LocalDateTime.now().minusDays(5), PetriNetRow.Status.IN_PROGRESS, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 7", "Grace", LocalDateTime.now().minusDays(6), PetriNetRow.Status.WAITING, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 8", "Heidi", LocalDateTime.now().minusDays(7), PetriNetRow.Status.WAITING, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 9", "Ivan", LocalDateTime.now().minusDays(8), PetriNetRow.Status.IN_PROGRESS, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 10", "Judy", LocalDateTime.now().minusDays(9), PetriNetRow.Status.COMPLETED, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 11", "Karl", LocalDateTime.now().minusDays(10), PetriNetRow.Status.IN_PROGRESS, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 12", "Leo", LocalDateTime.now().minusDays(11), PetriNetRow.Status.STARTED, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 13", "Mia", LocalDateTime.now().minusDays(12), PetriNetRow.Status.COMPLETED, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 14", "Nina", LocalDateTime.now().minusDays(13), PetriNetRow.Status.WAITING, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 15", "Oscar", LocalDateTime.now().minusDays(14), PetriNetRow.Status.WAITING, NetCategory.SUBSCRIBED),
                new PetriNetRow("Subscribed Net 16", "Paul", LocalDateTime.now().minusDays(15), PetriNetRow.Status.WAITING, NetCategory.SUBSCRIBED)
        );
    }

    //TODO
    private List<PetriNetRow> OwnSet() {
        return Arrays.asList(
                new PetriNetRow("Owned Net 1", "Alice", LocalDateTime.now(), PetriNetRow.Status.WAITING, NetCategory.OWNED),
                new PetriNetRow("Owned Net 2", "Bob", LocalDateTime.now().minusDays(1), PetriNetRow.Status.COMPLETED, NetCategory.OWNED),
                new PetriNetRow("Owned Net 3", "Charlie", LocalDateTime.now().minusDays(2), PetriNetRow.Status.IN_PROGRESS, NetCategory.OWNED),
                new PetriNetRow("Owned Net 4", "David", LocalDateTime.now().minusDays(3), PetriNetRow.Status.STARTED, NetCategory.OWNED),
                new PetriNetRow("Owned Net 5", "Eve", LocalDateTime.now().minusDays(4), PetriNetRow.Status.STARTED, NetCategory.OWNED)
        );
    }




}
