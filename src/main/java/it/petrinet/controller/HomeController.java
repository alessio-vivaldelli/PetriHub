package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.view.components.PetriNetTableComponent;
import it.petrinet.model.NetCategory;
import it.petrinet.model.PetriNetRow;
import it.petrinet.model.PetriNetRow.Status;
import it.petrinet.model.User;
import it.petrinet.model.database.UserDAO;
import it.petrinet.view.ViewNavigator;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

import javax.swing.text.View;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * Controller for the Home view, managing the main dashboard interface.
 * Handles user information display, recent nets table, and navigation.
 */
public class HomeController {

    private static final String GUEST_WELCOME_MESSAGE = "Welcome, Guest!";
    private static final String USER_WELCOME_FORMAT = "Welcome, %s!";

    // Dashboard statistics //TODO: Update with actual data
    @FXML private Label ownedNetsLabel ;
    @FXML private Label discoverableNetsLabel;
    @FXML private Label subscribedNetsLabel;

    @FXML private Button newNetButton;

    @FXML private VBox activityFeedContainer;

    // User information
    @FXML private Label userNameLabel;

    // Table container for the component
    @FXML private VBox tableContainer;

    // === Component ===
    private PetriNetTableComponent petriNetTable;


    // === Initialization ===

    @FXML
    private void initialize() throws InputTypeException {
        initializeUserInterface();
        initializeTableComponent();
        loadInitialData();
    }

    /**
     * Initialize user interface elements with current user information
     */
    private void initializeUserInterface() {
        User authenticatedUser = ViewNavigator.getAuthenticatedUser();
        setNewNetButton(authenticatedUser.isAdmin());
        updateWelcomeMessage(authenticatedUser);
    }

    private void setNewNetButton(boolean isEnabled) {
        newNetButton.setText("New Net ");
        IconUtils.setIcon(newNetButton, "add.png", 30, 30 , Color.BLACK, ContentDisplay.RIGHT);

        newNetButton.setVisible(isEnabled);
        newNetButton.setManaged(isEnabled);

    }

    /**
     * Initialize the table component and add it to the container
     */
    private void initializeTableComponent() {
        petriNetTable = new PetriNetTableComponent();

        // Set up row click handler
        petriNetTable.setOnRowClickHandler(this::handleTableRowClick);

        // Add the table component to the container
        tableContainer.getChildren().add(petriNetTable);
    }

    /**
     * Update welcome message based on authenticated user
     */
    private void updateWelcomeMessage(User user) {
        String welcomeText = (user != null)
                ? String.format(USER_WELCOME_FORMAT, user.getUsername())
                : GUEST_WELCOME_MESSAGE;
        userNameLabel.setText(welcomeText);
    }

    /**
     * Load initial data into the interface
     */
    private void loadInitialData() throws InputTypeException {
        populateRecentNetsTable();
        updateDashboardStatistics();
    }

    //TODO: Replace with actual data service calls
    private void updateDashboardStatistics() throws InputTypeException {
        int numberOfNets = UserDAO.getNumberOfOwnedNetsByUser(ViewNavigator.getAuthenticatedUser());
        if(numberOfNets < 0){
            ownedNetsLabel.setText("Error");
        }
        else{
            ownedNetsLabel.setText(String.valueOf(numberOfNets));
        }
        numberOfNets = PetriNetsDAO.getUnknownNetsByUser(ViewNavigator.getAuthenticatedUser()).size();
        if(numberOfNets < 0){
            discoverableNetsLabel.setText("Error");
        }
        else{discoverableNetsLabel.setText(String.valueOf(numberOfNets));}

        numberOfNets = UserDAO.getNumberOfSubscribedNetsByUser(ViewNavigator.getAuthenticatedUser());
        if(numberOfNets < 0){
            subscribedNetsLabel.setText("Error");
        }
        else{
            subscribedNetsLabel.setText(String.valueOf(numberOfNets));
        }



    }

    // === Event Handlers ===

    /**
     * Handle table row clicks
     */
    private void handleTableRowClick(PetriNetRow selectedNet) {
        NetCategory category = selectedNet.typeProperty().get();
        switch (category) {
            case OWNED -> ViewNavigator.navigateToUserList(selectedNet.nameProperty().get());
            case SUBSCRIBED -> ViewNavigator.navigateToSubNets();
            case DISCOVER -> ViewNavigator.navigateToDiscover();
            default -> {
                // Handle other categories or do nothing
                System.out.println("Clicked on net: " + selectedNet.nameProperty().get());
            }
        }
    }

    // === Data Management ===

    /**
     * Populate the recent nets table with sample data
     * TODO: Replace with actual data service calls
     */
    private void populateRecentNetsTable() {
        List<PetriNetRow> sampleData = createSampleNetData();
        petriNetTable.setData(sampleData);
    }

    /**
     * Create sample data for demonstration purposes
     * TODO: Replace with actual data loading from service/repository
     */
    private List<PetriNetRow> createSampleNetData() {
        return Arrays.asList(
                PetriNetRow.of("Manufacturing Process Flow", "Alice Johnson",
                        LocalDateTime.now().minusDays(1), Status.COMPLETED, NetCategory.SUBSCRIBED),
                PetriNetRow.of("Supply Chain Logistics", "Bob Smith",
                        LocalDateTime.now().minusDays(2), Status.IN_PROGRESS, NetCategory.OWNED),
                PetriNetRow.of("Server Request Model", "Charlie Brown",
                        LocalDateTime.now().minusDays(3), Status.STARTED, NetCategory.SUBSCRIBED),
                PetriNetRow.of("Inventory Management", "David Wilson",
                        LocalDateTime.now().minusDays(4), Status.WAITING, NetCategory.OWNED)
        );
    }

    public void clearTable() {
        petriNetTable.clearData();
    }

    public void handleSubscribedNetsClick(MouseEvent mouseEvent) {
        ViewNavigator.navigateToMyNets();
    }

    public void handleDiscoverableNetsClick(MouseEvent mouseEvent) {
        ViewNavigator.navigateToDiscover();
    }

    public void handleOwnedNetsClick(MouseEvent mouseEvent) {
        ViewNavigator.navigateToMyNets();
    }

    public void handleNewNetClick(ActionEvent event) {
        ViewNavigator.navigateToNetCreation();
    }
}