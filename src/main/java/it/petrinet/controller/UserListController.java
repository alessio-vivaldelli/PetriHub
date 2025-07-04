package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.ComputationRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.table.UserSelectComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UserListController {

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private UserSelectComponent userTable;
    private static String netID;

    public static void setNetID(String netID) {
        UserListController.netID = netID;
    }

    public void initialize() throws InputTypeException {
        frameTitle.setText(netID);
        IconUtils.setIcon(frameTitle, "user.png", 30, 30 , null);

        initializeTableComponent();
        loadSubUserData();
    }

    private  void initializeTableComponent() {
        userTable = new UserSelectComponent();
        userTable.setOnRowClickHandler(this::handleTableRowClick);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        tableContainer.getChildren().add(userTable);
    }

    //TODO: Handle row click event
    private void handleTableRowClick(ComputationRow computationRow) {

    }

    private void loadSubUserData() throws InputTypeException {
        List<ComputationRow> subUsers = fetchSubUsers();
        userTable.setData(subUsers);
    }

    private List<ComputationRow> fetchSubUsers() throws InputTypeException {
        List <Computation> wantedComputation = ComputationsDAO.getComputationsByNet(PetriNetsDAO.getNetByName(netID.trim()));

        ArrayList<ComputationRow> Comps = new ArrayList<ComputationRow>();

        for (Computation c: wantedComputation){
            Comps.add(new ComputationRow(ComputationsDAO.getIdByComputation(c)+"",c.getUserId(), LocalDateTime.now(),LocalDateTime.now().plusHours(1),Status.COMPLETED));
        }

        return Comps;
    }


}
