
// Base generic table component
package it.petrinet.view.components.table;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;

import java.util.List;
import java.util.function.Consumer;

/**
 * Generic base class for table components
 * @param <T> The type of data displayed in the table
 */
public abstract class GenericTableComponent<T> extends BaseTableComponent {

    @FXML protected TableView<T> tableView;

    protected Consumer<T> onRowClickHandler;
    protected ObservableList<T> tableData;

    @Override
    public void initialize() {
        setupTableColumns();
        setupTableBehavior();
        initializeData();
    }

    /**
     * Setup table columns - to be implemented by subclasses
     */
    protected abstract void setupTableColumns();

    /**
     * Configure table row behavior and interactions
     */
    protected void setupTableBehavior() {
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>();
            row.setOnMouseClicked(this::handleTableRowClick);
            return row;
        });
    }

    /**
     * Initialize data container
     */
    protected void initializeData() {
        tableData = FXCollections.observableArrayList();
        tableView.setItems(tableData);
    }

    /**
     * Handle mouse clicks on table rows
     */
    protected void handleTableRowClick(MouseEvent event) {
        @SuppressWarnings("unchecked")
        TableRow<T> source = (TableRow<T>) event.getSource();

        if (event.getClickCount() == 1 && !source.isEmpty()) {
            T selectedItem = source.getItem();
            if (onRowClickHandler != null) {
                onRowClickHandler.accept(selectedItem);
            }
        }
    }

    // === Public API Methods ===

    /**
     * Set the data for the table
     */
    public void setData(List<T> data) {
        tableData.clear();
        if (data != null) {
            tableData.addAll(data);
        }
    }

    /**
     * Add a single item to the table
     */
    public void addItem(T item) {
        if (item != null) {
            tableData.add(item);
        }
    }

    /**
     * Add multiple items to the table
     */
    public void addItems(List<T> items) {
        if (items != null) {
            tableData.addAll(items);
        }
    }

    /**
     * Remove an item from the table
     */
    public void removeItem(T item) {
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
     */
    public T getSelectedItem() {
        return tableView.getSelectionModel().getSelectedItem();
    }

    /**
     * Set the handler for row click events
     */
    public void setOnRowClickHandler(Consumer<T> handler) {
        this.onRowClickHandler = handler;
    }

    /**
     * Get all data currently in the table
     */
    public ObservableList<T> getData() {
        return FXCollections.unmodifiableObservableList(tableData);
    }

    /**
     * Refresh the table view
     */
    public void refresh() {
        tableView.refresh();
    }

    /**
     * Get the underlying TableView
     */
    public TableView<T> getTableView() {
        return tableView;
    }
}