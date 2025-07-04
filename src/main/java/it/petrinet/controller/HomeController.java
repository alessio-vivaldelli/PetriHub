package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.model.TableRow.PetriNetRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.model.User;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.model.database.RecentNet;
import it.petrinet.model.database.UserDAO;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.table.DynamicPetriNetTableComponent;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import it.petrinet.utils.NavigationHelper;
import static it.petrinet.utils.Safenavigate.safeNavigate;

public class HomeController implements Initializable {

    private static final Logger LOGGER = Logger.getLogger(HomeController.class.getName());

    // Constants
    private static final String GUEST_WELCOME_MESSAGE = "Welcome, Guest!";
    private static final String USER_WELCOME_FORMAT = "Welcome, %s!";
    private static final String ERROR_DISPLAY = "Error";
    private static final String NEW_NET_BUTTON_TEXT = "New Net";
    private static final String ADD_ICON_PATH = "add.png";
    private static final int ICON_SIZE = 30;
    private static final int RECENT_NETS_LIMIT = 2;

    // FXML Components
    @FXML private Label ownedNetsLabel;
    @FXML private Label discoverableNetsLabel;
    @FXML private Label subscribedNetsLabel;
    @FXML private Button newNetButton;
    @FXML private VBox activityFeedContainer;
    @FXML private Label userNameLabel;
    @FXML private VBox tableContainer;
    @FXML private VBox ownedStats;

    // State - CRITICAL: Make these static to persist across scene transitions
    private static DynamicPetriNetTableComponent petriNetTable;
    private static boolean isGloballyInitialized = false;
    private boolean isThisInstanceInitialized = false;
    private User currentUser;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        LOGGER.info("HomeController.initialize() called");
        try {
            initializeController();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize HomeController", e);
            showErrorState();
        }
    }

    /**
     * Main initialization method that sets up the entire controller
     */
    private void initializeController() {
        if (isThisInstanceInitialized) {
            LOGGER.info("Controller already initialized for this instance");
            return;
        }

        currentUser = ViewNavigator.getAuthenticatedUser();
        initializeUserInterface();
        initializeTableComponent();
        loadInitialData();
        isThisInstanceInitialized = true;

        LOGGER.info("Controller initialized successfully");
    }

    /**
     * Sets up the user interface components
     */
    private void initializeUserInterface() {
        configureNewNetButton();
        updateWelcomeMessage();
    }

    /**
     * Configures the new net button based on user permissions
     */
    private void configureNewNetButton() {
        boolean isAdmin = currentUser != null && currentUser.isAdmin();

        newNetButton.setText(NEW_NET_BUTTON_TEXT);

        if (isAdmin) {
            IconUtils.setIcon(newNetButton, ADD_ICON_PATH, ICON_SIZE, ICON_SIZE,
                    Color.BLACK, ContentDisplay.RIGHT);
        }

        newNetButton.setVisible(isAdmin);
        newNetButton.setManaged(isAdmin);
        ownedStats.setVisible(isAdmin);
        ownedStats.setManaged(isAdmin);
    }

    /**
     * CRITICAL: Initialize table component with global state management
     */
    private void initializeTableComponent() {
        LOGGER.info("Initializing table component...");

        if (petriNetTable == null || !isGloballyInitialized) {
            LOGGER.info("Creating new table component");
            petriNetTable = new DynamicPetriNetTableComponent();
            petriNetTable.setOnRowClickHandler(this::handleTableRowClick);
            isGloballyInitialized = true;
        } else {
            LOGGER.info("Reusing existing table component");
        }

        // Always clear and re-add to current container
        tableContainer.getChildren().clear();
        VBox.setVgrow(petriNetTable, Priority.ALWAYS);
        tableContainer.getChildren().add(petriNetTable);

        LOGGER.info("Table component setup completed");
    }

    /**
     * Updates the welcome message based on current user
     */
    private void updateWelcomeMessage() {
        String welcomeText = (currentUser != null && currentUser.getUsername() != null)
                ? String.format(USER_WELCOME_FORMAT, currentUser.getUsername())
                : GUEST_WELCOME_MESSAGE;
        userNameLabel.setText(welcomeText);
    }

    /**
     * Loads initial data for the dashboard
     */
    private void loadInitialData() {
        try {
            refreshTableData();
            updateDashboardStatistics();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to load initial data", e);
            showErrorInLabels();
        }
    }

    /**
     * Updates dashboard statistics labels
     */
    private void updateDashboardStatistics() {
        if (currentUser == null) {
            setAllLabelsToZero();
            return;
        }

        updateOwnedNetsCount();
        updateDiscoverableNetsCount();
        updateSubscribedNetsCount();
    }

    /**
     * Updates owned nets count label
     */
    private void updateOwnedNetsCount() {
        try {
            int count = UserDAO.getNumberOfOwnedNetsByUser(currentUser);
            ownedNetsLabel.setText(count < 0 ? ERROR_DISPLAY : String.valueOf(count));
        } catch (InputTypeException e) {
            LOGGER.log(Level.WARNING, "Failed to get owned nets count", e);
            ownedNetsLabel.setText(ERROR_DISPLAY);
        }
    }

    /**
     * Updates discoverable nets count label
     */
    private void updateDiscoverableNetsCount() {
        try {
            int count = PetriNetsDAO.getUnknownNetsByUser(currentUser).size();
            discoverableNetsLabel.setText(String.valueOf(count));
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get discoverable nets count", e);
            discoverableNetsLabel.setText(ERROR_DISPLAY);
        }
    }

    /**
     * Updates subscribed nets count label
     */
    private void updateSubscribedNetsCount() {
        try {
            int count = UserDAO.getNumberOfSubscribedNetsByUser(currentUser);
            subscribedNetsLabel.setText(count < 0 ? ERROR_DISPLAY : String.valueOf(count));
        } catch (InputTypeException e) {
            LOGGER.log(Level.WARNING, "Failed to get subscribed nets count", e);
            subscribedNetsLabel.setText(ERROR_DISPLAY);
        }
    }

    /**
     * CRITICAL: Refresh table data without recreating structure
     */
    public void refreshTableData() throws InputTypeException {
        LOGGER.info("Refreshing table data...");

        if (petriNetTable == null) {
            LOGGER.warning("Table is null, reinitializing...");
            initializeTableComponent();
        }

        List<PetriNetRow> recentNets = buildRecentNetsList();
        petriNetTable.setData(recentNets);

        LOGGER.info("Table data refreshed with " + recentNets.size() + " items");
    }

    /**
     * Builds the list of recent nets for display
     */
    private List<PetriNetRow> buildRecentNetsList() throws InputTypeException {
        List<PetriNetRow> recentNets = new ArrayList<>();
        List<RecentNet> wantedNets = PetriNetsDAO.getMostRecentlyModifiedNets(
                ViewNavigator.getAuthenticatedUser(), RECENT_NETS_LIMIT);

        for (RecentNet net : wantedNets) {
            if (net != null) {
                NetCategory category = determineNetCategory(net);
                LocalDateTime date = determineNetDate(net);

                recentNets.add(new PetriNetRow(
                        net.getNet().getNetName(),
                        net.getNet().getCreatorId(),
                        date,
                        Status.NOT_STARTED,
                        category
                ));
            }
        }
        return recentNets;
    }

    /**
     * Determines the category of a net for current user
     */
    private NetCategory determineNetCategory(RecentNet net) {
        String currentUsername = ViewNavigator.getAuthenticatedUser().getUsername();
        return net.getNet().getCreatorId().equals(currentUsername)
                ? NetCategory.OWNED : NetCategory.SUBSCRIBED;
    }

    /**
     * Determines the appropriate date for a net
     */
    private LocalDateTime determineNetDate(RecentNet net) {
        if (net.getTimestamp() == null || net.getTimestamp() <= 0L) {
            return LocalDateTime.ofEpochSecond(net.getNet().getCreationDate(), 0, ZoneOffset.UTC);
        } else {
            return LocalDateTime.ofEpochSecond(net.getTimestamp(), 0, ZoneOffset.UTC);
        }
    }

    /**
     * Handles clicks on table rows with appropriate navigation
     */
    private void handleTableRowClick(PetriNetRow selectedNet) {
        if (selectedNet == null) return;

        NetCategory category = selectedNet.typeProperty().get();
        String netName = selectedNet.nameProperty().get();

        try {
            switch (category) {
                case OWNED -> safeNavigate(() -> ViewNavigator.navigateToUserList(netName));
                case SUBSCRIBED -> handleSubscribedNetClick(netName);
                case DISCOVER -> safeNavigate(ViewNavigator::navigateToDiscover);
                default -> LOGGER.info("Clicked on net: " + netName);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Navigation failed for net: " + netName, e);
        }
    }

    /**
     * Handles clicks on subscribed nets
     */
    private void handleSubscribedNetClick(String netName) {
        try {
            PetriNet net = PetriNetsDAO.getNetByName(netName);
            if (net != null) {
                setupNavigationToNetVisual(net);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to handle subscribed net click", e);
        }
    }

    /**
     * Sets up navigation to net visual view
     */
    private void setupNavigationToNetVisual(PetriNet net) throws InputTypeException {
        NavigationHelper.setupNavigationToNetVisualForCurrentUser(net);
    }

    /**
     * Finds computation data for current user and net
     */
    private Computation findUserComputation(PetriNet net) throws InputTypeException {
        return NavigationHelper.findUserComputation(net, ViewNavigator.getAuthenticatedUser().getUsername());
    }

    /**
     * CRITICAL: Public method to refresh the entire dashboard
     */
    public void refreshDashboard() {
        LOGGER.info("Refreshing dashboard...");
        Platform.runLater(() -> {
            currentUser = ViewNavigator.getAuthenticatedUser();
            updateWelcomeMessage();
            configureNewNetButton();
            loadInitialData();
        });
    }

    /**
     * CRITICAL: Clean up method to reset global state
     */
    public static void resetGlobalState() {
        LOGGER.info("Resetting global state...");
        petriNetTable = null;
        isGloballyInitialized = false;
    }

    // Error handling methods
    private void showErrorState() {
        userNameLabel.setText("Error loading dashboard");
        showErrorInLabels();
    }

    private void showErrorInLabels() {
        ownedNetsLabel.setText(ERROR_DISPLAY);
        discoverableNetsLabel.setText(ERROR_DISPLAY);
        subscribedNetsLabel.setText(ERROR_DISPLAY);
    }

    private void setAllLabelsToZero() {
        ownedNetsLabel.setText("0");
        discoverableNetsLabel.setText("0");
        subscribedNetsLabel.setText("0");
    }

    // FXML Event Handlers
    @FXML
    public void clearTable() {
        if (petriNetTable != null) {
            petriNetTable.clearData();
        }
    }

    @FXML
    public void handleSubscribedNetsClick(MouseEvent event) {
        safeNavigate(ViewNavigator::navigateToSubNets);
    }

    @FXML
    public void handleDiscoverableNetsClick(MouseEvent event) {
        safeNavigate(ViewNavigator::navigateToDiscover);
    }

    @FXML
    public void handleOwnedNetsClick(MouseEvent event) {
        safeNavigate(ViewNavigator::navigateToMyNets);
    }

    @FXML
    public void handleNewNetClick(ActionEvent event) {
        safeNavigate(ViewNavigator::navigateToNetCreation);
    }

    // Getters for testing purposes
    public boolean isInitialized() {
        return isThisInstanceInitialized;
    }

    public User getCurrentUser() {
        return currentUser;
    }

    public static DynamicPetriNetTableComponent getPetriNetTable() {
        return petriNetTable;
    }
}