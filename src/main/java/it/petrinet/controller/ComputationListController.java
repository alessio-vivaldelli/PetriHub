package it.petrinet.controller;

import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.ComputationRow;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.utils.IconUtils;
import it.petrinet.utils.NavigationHelper;
import it.petrinet.view.components.table.ComputationSelectComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.petrinet.utils.NetStatusGetter.determineNetDate;
import static it.petrinet.utils.NetStatusGetter.getStatusByComputation;

public class ComputationListController {

    private static final Logger LOGGER = Logger.getLogger(ComputationListController.class.getName());

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private ComputationSelectComponent userTable;
    private String netID;

    public void initData(String netID) {
        this.netID = Objects.requireNonNull(netID, "Category cannot be null");

        initializeUserInterface();
        initializeTableComponent();
        loadSubUserData();
    }

    /**
     * Initializes the user interface components
     */
    private void initializeUserInterface() {
        frameTitle.setText(netID);
        IconUtils.setIcon(frameTitle, "user.png", 30, 30, null);
    }

    /**
     * Initializes the table component
     */
    private void initializeTableComponent() {
        userTable = new ComputationSelectComponent();
        userTable.setOnRowClickHandler(this::handleTableRowClick);
        VBox.setVgrow(userTable, Priority.ALWAYS);
        tableContainer.getChildren().add(userTable);
    }


    /**
     * Handles table row click events - navigates to computation view
     */
    private void handleTableRowClick(ComputationRow computationRow) {
        if (computationRow == null) return;

        try {
            PetriNet net = PetriNetsDAO.getNetByName(netID.trim());
            if (net != null) {
                String userId = computationRow.usernameProperty().get();
                setupNavigationToNetVisual(net, userId);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to handle table row click", e);
        }
    }

    /**
     * Sets up navigation to net visual view for specific user
     */
    private void setupNavigationToNetVisual(PetriNet net, String userId)  {
        NavigationHelper.setupNavigationToNetVisualForUser(net, userId);
    }

    /**
     * Finds computation data for specific user and net
     */
    private Computation findUserComputation(PetriNet net, String userId)  {
        return NavigationHelper.findUserComputation(net, userId);
    }

    /**
     * Loads subscriber user data for the table
     */
    private void loadSubUserData()  {
        List<ComputationRow> subUsers = fetchSubUsers();
        userTable.setData(subUsers);
    }

    /**
     * Fetches subscriber users and their computation data
     */
    private List<ComputationRow> fetchSubUsers()  {
        PetriNet net = PetriNetsDAO.getNetByName(netID.trim());
        if (net == null) {
            LOGGER.warning("Net not found: " + netID);
            return new ArrayList<>();
        }

        List<Computation> computations = ComputationsDAO.getComputationsByNet(net);

        List<ComputationRow> computationRows = new ArrayList<>();

        System.out.println(computations.size());

        for (Computation computation : computations) {
            int computationId = ComputationsDAO.getIdByComputation(computation);

            computationRows.add(new ComputationRow(
                    String.valueOf(computationId),
                    computation.getUserId(),
                    computation.getStartDate(),
                    determineNetDate(computation, computation.getEnd()),
                    getStatusByComputation(computation)
            ));
        }
        return computationRows;
    }
}
