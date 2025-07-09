package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.model.TableRow.PetriNetRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.utils.IconUtils;
import it.petrinet.utils.NavigationHelper;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.table.PetriNetTableComponent;
import it.petrinet.model.Computation.NEXT_STEP_TYPE;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;


import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.petrinet.utils.Safenavigate.safeNavigate;
import static it.petrinet.utils.StatusByComputation.determineNetDate;
import static it.petrinet.utils.StatusByComputation.getStatusByComputation;

public class ShowAllController {

    private static final Logger LOGGER = Logger.getLogger(ShowAllController.class.getName());

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private PetriNetTableComponent petriNetTable;
    private static NetCategory cardType;

    public static void setType(NetCategory type) {
        cardType = type;
    }

    @FXML
    public void initialize() throws InputTypeException {
        initializeTableComponent();
        initializeUserInterface();
        loadShowAllData();
    }

    /**
     * Initializes the user interface components
     */
    private void initializeUserInterface() {
        frameTitle.setText(" " + cardType.getDisplayName());
        IconUtils.setIcon(frameTitle, cardType.getDisplayName(), 30, 30, Color.WHITE);
        if(cardType == NetCategory.SUBSCRIBED) petriNetTable.dataColSubName();
    }

    /**
     * Initializes the table component and sets up event handlers
     */
    private void initializeTableComponent() {
        petriNetTable = new PetriNetTableComponent();
        setupTableEventHandlers();

        VBox.setVgrow(petriNetTable, Priority.ALWAYS);
        tableContainer.getChildren().add(petriNetTable);
    }

    /**
     * Sets up event handlers based on card type
     */
    private void setupTableEventHandlers() {
        if (cardType == NetCategory.OWNED) petriNetTable.setOnRowClickHandler(this::handleUserTableView);
        else petriNetTable.setOnRowClickHandler(this::handleTableRowClick);
    }

    /**
     * Handles clicks on owned nets - navigates to user list
     */
    private void handleUserTableView(PetriNetRow petriNetRow) {
        String netName = petriNetRow.nameProperty().get();
        safeNavigate(() -> ViewNavigator.navigateToUserList(netName));
    }

    /**
     * Handles clicks on subscribed/discover nets
     */
    private void handleTableRowClick(PetriNetRow petriNetRow) {
        try {
            String netName = petriNetRow.nameProperty().get();
            PetriNet net = PetriNetsDAO.getNetByName(netName);

            if (net == null) {
                LOGGER.warning("Net not found: " + netName);
                return;
            }

            switch (cardType) {
                case SUBSCRIBED -> setupNavigationToNetVisual(net, ViewNavigator.getAuthenticatedUser().getUsername());
                case DISCOVER -> setupNavigationToNetDiscover(net, ViewNavigator.getAuthenticatedUser().getUsername());
                default -> throw new InputTypeException();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling table row click", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Sets up navigation to net visual view for subscribed nets
     */
    private void setupNavigationToNetVisual(PetriNet net, String userId) throws InputTypeException {
        NavigationHelper.setupNavigationToNetVisualForUser(net, userId);
    }

    /**
     * Sets up navigation to net discover view for discoverable nets
     */
    private void setupNavigationToNetDiscover(PetriNet net, String username) {
        NavigationHelper.setupNavigationToTableDiscoverForUser(net, username);
    }

    /**
     * Finds computation data for current user and net
     */
    private Computation findUserComputation(PetriNet net) throws InputTypeException {
        return NavigationHelper.findUserComputation(net, ViewNavigator.getAuthenticatedUser().getUsername());
    }

    /**
     * Loads data based on card type
     */
    private void loadShowAllData() throws InputTypeException {
        List<PetriNetRow> allNets = switch (cardType) {
            case OWNED -> getOwnedNets();
            case SUBSCRIBED -> getSubscribedNets();
            case DISCOVER -> getDiscoverableNets();
            default -> throw new InputTypeException();
        };
        petriNetTable.setData(allNets);
    }

    /**
     * Gets discoverable nets for current user
     */
    private List<PetriNetRow> getDiscoverableNets() throws InputTypeException {
        List<PetriNet> unknownNets = PetriNetsDAO.getDiscoverableNetsByUser(ViewNavigator.getAuthenticatedUser());
        return makeList(unknownNets, NetCategory.DISCOVER);
    }

    /**
     * Gets subscribed nets for current user
     */
    private List<PetriNetRow> getSubscribedNets() throws InputTypeException {
        List<PetriNet> subscribedNets = ComputationsDAO.getNetsSubscribedByUser(
                ViewNavigator.getAuthenticatedUser());
        return makeList(subscribedNets, NetCategory.SUBSCRIBED);
    }

    /**
     * Gets owned nets for current user
     */
    private List<PetriNetRow> getOwnedNets() throws InputTypeException {
        List<PetriNet> ownNets = PetriNetsDAO.getNetsByCreator(ViewNavigator.getAuthenticatedUser());
        return makeList(ownNets, NetCategory.OWNED);
    }

    private List<PetriNetRow> makeList(List<PetriNet> nets, NetCategory category) throws InputTypeException {
        List<PetriNetRow> netsToShow = new ArrayList<>();
        for (PetriNet net : nets) {
            Computation computation = (category == NetCategory.OWNED) ?
                    getFirstSubscribedNets(net) :
                    findUserComputation(net);
            if (!(net.getNetName() == null)){
                netsToShow.add(new PetriNetRow(
                        net.getNetName(),
                        net.getCreatorId(),
                        determineNetDate(computation, net.getCreationDate()),
                        getStatusByComputation(computation),
                        category
                ));
            }
        }
        return netsToShow;
    }

    private Computation getFirstSubscribedNets(PetriNet net) throws InputTypeException {
        ComputationStep step = ComputationStepDAO.getLastComputationStepForPetriNet(net.getNetName());
        return (step != null) ? ComputationStepDAO.getComputationByStep(step) : null;
    }
}