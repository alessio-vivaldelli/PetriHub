package it.petrinet.view.components;

import it.petrinet.model.NetCategory;
import it.petrinet.model.PetriNetRow;
import it.petrinet.model.PetriNetRow.Status;
import it.petrinet.utils.BadgeCell;
import it.petrinet.utils.BadgeCell.Category;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable PetriNet Table Component
 * Can be used in any view that needs to display PetriNet data in a table format
 */
public class PetriNetTableComponent extends VBox {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // === FXML Components ===
    @FXML private TableView<PetriNetRow> tableView;
    @FXML private TableColumn<PetriNetRow, String> nameCol;
    @FXML private TableColumn<PetriNetRow, String> authorCol;
    @FXML private TableColumn<PetriNetRow, LocalDateTime> dateCol;
    @FXML private TableColumn<PetriNetRow, Status> statusCol;
    @FXML private TableColumn<PetriNetRow, NetCategory> typeCol;

    // === Properties ===
    private Consumer<PetriNetRow> onRowClickHandler;
    private ObservableList<PetriNetRow> tableData;


    // === Constructor ===
    public PetriNetTableComponent() {
        loadFXML();
        initialize();
    }

    /**
     * Load the FXML file for this component
     */
    private void loadFXML() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/components/PetriNetTable.fxml"));
            loader.setRoot(this);
            loader.setController(this);
            loader.load();
        } catch (IOException e) {
            throw new RuntimeException("Could not load PetriNetTable.fxml", e);
        }
    }

    /**
     * Initialize the component after FXML loading
     */
    @FXML
    private void initialize() {
        setupTableColumns();
        setupTableBehavior();
        initializeData();
    }

    /**
     * Configure table columns with appropriate cell value factories and formatters
     */
    private void setupTableColumns() {
        // Basic columns
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorProperty());

        // Date column with custom formatting
        setupDateColumn();

        // Badge columns for status and type
        statusCol.setCellFactory(column -> new BadgeCell<>(Category.STATUS));
        typeCol.setCellFactory(column -> new BadgeCell<>(Category.TYPE));
    }

    /**
     * Setup date column with custom cell factory for formatting
     */
    private void setupDateColumn() {
        dateCol.setCellFactory(column -> new TableCell<PetriNetRow, LocalDateTime>() {
            @Override
            protected void updateItem(LocalDateTime dateTime, boolean empty) {
                super.updateItem(dateTime, empty);
                setText(empty || dateTime == null ? null : dateTime.format(DATE_FORMAT));
            }
        });
    }

    /**
     * Configure table row behavior and interactions
     */
    private void setupTableBehavior() {
        tableView.setRowFactory(tableView -> {
            TableRow<PetriNetRow> row = new TableRow<>();
            row.setOnMouseClicked(this::handleTableRowClick);
            return row;
        });
    }

    /**
     * Initialize data container
     */
    private void initializeData() {
        tableData = FXCollections.observableArrayList();
        tableView.setItems(tableData);
    }

    // === Event Handlers ===

    /**
     * Handle mouse clicks on table rows
     */
    private void handleTableRowClick(MouseEvent event) {
        TableRow<PetriNetRow> source = (TableRow<PetriNetRow>) event.getSource();

        if (event.getClickCount() == 1 && !source.isEmpty()) {
            PetriNetRow selectedItem = source.getItem();
            if (onRowClickHandler != null) {
                onRowClickHandler.accept(selectedItem);
            }
        }
    }

    // === Public API Methods ===

    /**
     * Set the data for the table
     * @param data List of PetriNetRow items to display
     */
    public void setData(List<PetriNetRow> data) {
        tableData.clear();
        if (data != null) {
            tableData.addAll(data);
        }
    }

    /**
     * Add a single item to the table
     * @param item PetriNetRow to add
     */
    public void addItem(PetriNetRow item) {
        if (item != null) {
            tableData.add(item);
        }
    }

    /**
     * Add multiple items to the table
     * @param items List of PetriNetRow items to add
     */
    public void addItems(List<PetriNetRow> items) {
        if (items != null) {
            tableData.addAll(items);
        }
    }

    /**
     * Remove an item from the table
     * @param item PetriNetRow to remove
     */
    public void removeItem(PetriNetRow item) {
        tableData.remove(item);
    }

    /**
     * Clear all data from the table
     */
    public void clearData() {
        tableData.clear();
    }

    /**
     * Get the currently selected item
     * @return Selected PetriNetRow or null if none selected
     */
    public PetriNetRow getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    /**
     * Set the handler for row click events
     * @param handler Consumer that will be called when a row is clicked
     */
    public void setOnRowClickHandler(Consumer<PetriNetRow> handler) {
        this.onRowClickHandler = handler;
    }

    /**
     * Get all data currently in the table
     * @return ObservableList of all table data
     */
    public ObservableList<PetriNetRow> getData() {
        return FXCollections.unmodifiableObservableList(tableData);
    }

    /**
     * Refresh the table view
     */
    public void refresh() {
        tableView.refresh();
    }

    /**
     * Get the underlying TableView (for advanced customization if needed)
     * @return The TableView component
     */
    public TableView<PetriNetRow> getTableView() {
        return tableView;
    }

}