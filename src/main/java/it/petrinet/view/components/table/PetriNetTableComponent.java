package it.petrinet.view.components.table;

import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.model.TableRow.PetriNetRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.utils.BadgeCell;
import it.petrinet.utils.TableColumnUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;

import java.time.LocalDateTime;

/**
 * PetriNet Table Component
 */
public class PetriNetTableComponent extends GenericTableComponent<PetriNetRow> {

    @FXML private TableColumn<PetriNetRow, String> nameCol;
    @FXML private TableColumn<PetriNetRow, String> authorCol;
    @FXML private TableColumn<PetriNetRow, LocalDateTime> dateCol;
    @FXML private TableColumn<PetriNetRow, Status> statusCol;
    @FXML private TableColumn<PetriNetRow, NetCategory> typeCol;

    @Override
    public String getFXMLPath() {
        return "/fxml/components/PetriNetSelectionList.fxml";
    }

    @Override
    protected void setupTableColumns() {
        // Basic columns
        nameCol.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        authorCol.setCellValueFactory(cellData -> cellData.getValue().authorProperty());

        // Date column with custom formatting
        TableColumnUtils.setupDateColumn(dateCol);

        // Badge columns for status and type
        statusCol.setCellValueFactory(new PropertyValueFactory<>("status"));
        statusCol.setCellFactory(column -> new BadgeCell<>(BadgeCell.Category.STATUS));
        typeCol.setCellFactory(column -> new BadgeCell<>(BadgeCell.Category.TYPE));
    }

    public void dataColSubName(){
        dateCol.setText("Last Update");
    }

}