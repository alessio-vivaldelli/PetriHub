package it.petrinet.controller;

import it.petrinet.model.TableRow.ComputationRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.components.table.UserSelectComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class UserListController {

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private  UserSelectComponent userTable;
    private static String netID;

    public static void setNetID(String netID) {
        UserListController.netID = netID;
    }

    public void initialize() {
        frameTitle.setText("SELECT NAME OF ID: " + netID);
        IconUtils.setIcon(frameTitle, "user.png", 30, 30 , null);

        initializeTableComponent();
        loadSubUserData();
    }

    private  void initializeTableComponent() {
        userTable = new UserSelectComponent();
        userTable.setOnRowClickHandler(this::handleTableRowClick);
        tableContainer.getChildren().add(userTable);
    }

    //TODO: Handle row click event
    private void handleTableRowClick(ComputationRow computationRow) {
        System.out.println("Row clicked: " + computationRow.usernameProperty().get());
        // Handle the row click event, e.g., navigate to a user details view or perform an action
    }

    private void loadSubUserData() {
        List<ComputationRow> subUsers = fetchSubUsers();
        userTable.setData(subUsers);
    }

    private List<ComputationRow> fetchSubUsers() {
        return Arrays.asList(
            new ComputationRow("1", "user1", LocalDateTime.now(), LocalDateTime.now().plusHours(1), Status.COMPLETED),
            new ComputationRow("2", "user2", LocalDateTime.now().minusDays(1), LocalDateTime.now(), Status.IN_PROGRESS),
            new ComputationRow("3", "user3", LocalDateTime.now().minusDays(2), LocalDateTime.now().minusHours(2), Status.STARTED)
        );
    }


}
