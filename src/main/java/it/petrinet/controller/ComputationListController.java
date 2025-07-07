package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.ComputationRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.utils.IconUtils;
import it.petrinet.utils.NavigationHelper;
import it.petrinet.view.components.table.ComputationSelectComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ComputationListController {

    private static final Logger LOGGER = Logger.getLogger(ComputationListController.class.getName());

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private ComputationSelectComponent userTable;
    private static String netID;

    public static void setNetID(String netID) {
        ComputationListController.netID = netID;
    }

    @FXML
    public void initialize() throws InputTypeException {
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
    private void setupNavigationToNetVisual(PetriNet net, String userId) throws InputTypeException {
        NavigationHelper.setupNavigationToNetVisualForUser(net, userId);
    }

    /**
     * Finds computation data for specific user and net
     */
    private Computation findUserComputation(PetriNet net, String userId) throws InputTypeException {
        return NavigationHelper.findUserComputation(net, userId);
    }

    /**
     * Loads subscriber user data for the table
     */
    private void loadSubUserData() throws InputTypeException {
        List<ComputationRow> subUsers = fetchSubUsers();
        userTable.setData(subUsers);
    }

    /**
     * Fetches subscriber users and their computation data
     */
    private List<ComputationRow> fetchSubUsers() throws InputTypeException {
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
            Status status = determineComputationStatus(computation);

            // Using placeholder dates - should be replaced with actual computation dates
            LocalDateTime startDate = LocalDateTime.now(); // TODO: Get actual start date
            LocalDateTime endDate = LocalDateTime.now().plusHours(1); // TODO: Get actual end date

            computationRows.add(new ComputationRow(
                    String.valueOf(computationId),
                    computation.getUserId(),
                    startDate,
                    endDate,
                    status
            ));
        }

        return computationRows;
    }

    /**
     * Determines the status of a computation
     */
    private Status determineComputationStatus(Computation computation) {
        if (!computation.isStarted()) {
            return Status.NOT_STARTED;
        } else if (computation.isFinished()) {
            return Status.COMPLETED;
        } else {
            // TODO: Implement notification check logic
            boolean hasNotifications = false;
            return hasNotifications ? Status.WAITING : Status.IN_PROGRESS;
        }
    }
}