package it.petrinet.view.components.table;

import it.petrinet.model.TableRow.ComputationRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.utils.BadgeCell;
import it.petrinet.utils.TableColumnUtils;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;

import java.time.LocalDateTime;

/**
 * User Select Table Component
 */
public class UserSelectComponent extends GenericTableComponent<ComputationRow> {

    @FXML private TableColumn<ComputationRow, String> IDCol;
    @FXML private TableColumn<ComputationRow, String> usernameCol;
    @FXML private TableColumn<ComputationRow, LocalDateTime> initTimeCol;
    @FXML private TableColumn<ComputationRow, LocalDateTime> endTimeCol;
    @FXML private TableColumn<ComputationRow, Status> statusCol;

    @Override
    public String getFXMLPath() {
        return "/fxml/components/UserSelectList.fxml";
    }

    @Override
    protected void setupTableColumns() {
        // Basic columns
        IDCol.setCellValueFactory(cellData -> cellData.getValue().IDProperty());
        usernameCol.setCellValueFactory(cellData -> cellData.getValue().usernameProperty());

        initTimeCol.setCellValueFactory(cellData -> cellData.getValue().initTimeProperty());
        endTimeCol.setCellValueFactory(cellData -> cellData.getValue().endTimeProperty());

        // Date columns
        TableColumnUtils.setupDateColumn(initTimeCol);
        TableColumnUtils.setupDateColumn(endTimeCol);

        // Status badge column
        statusCol.setCellFactory(column -> new BadgeCell<>(BadgeCell.Category.STATUS));
    }
}