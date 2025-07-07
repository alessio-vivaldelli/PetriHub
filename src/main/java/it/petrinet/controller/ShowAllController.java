package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.model.TableRow.PetriNetRow;
import it.petrinet.model.TableRow.Status;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.model.database.RecentNet;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.table.PetriNetTableComponent;
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

import it.petrinet.utils.NavigationHelper;
import static it.petrinet.utils.Safenavigate.safeNavigate;

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
        initializeUserInterface();
        initializeTableComponent();
        loadShowAllData();
    }

    /**
     * Initializes the user interface components
     */
    private void initializeUserInterface() {
        frameTitle.setText(" " + cardType.getDisplayName());
        IconUtils.setIcon(frameTitle, cardType.getDisplayName(), 30, 30, Color.WHITE);
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
        if (cardType == NetCategory.OWNED) {
            petriNetTable.setOnRowClickHandler(this::handleUserTableView);
        } else {
            petriNetTable.setOnRowClickHandler(this::handleTableRowClick);
        }
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
                case SUBSCRIBED -> setupNavigationToNetVisual(net);
                case DISCOVER -> LOGGER.info("Discover functionality not implemented yet");
                default -> throw new InputTypeException();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling table row click", e);
            throw new RuntimeException(e);
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
        List<RecentNet> unknownNets = PetriNetsDAO.getUnknownNetsByUser(ViewNavigator.getAuthenticatedUser());
        List<PetriNetRow> netsToShow = new ArrayList<>();

        for (RecentNet net : unknownNets) {
            if(!(net.getNet().getNetName() == null)){
                LocalDateTime creationDate = LocalDateTime.ofEpochSecond(net.getTimestamp(), 0, ZoneOffset.UTC);
                netsToShow.add(new PetriNetRow(
                        net.getNet().getNetName(),
                        net.getNet().getCreatorId(),
                        creationDate,
                        Status.NOT_STARTED,
                        NetCategory.DISCOVER
                ));
            }

        }
        return netsToShow;
    }

    /**
     * Gets subscribed nets for current user
     */
    private List<PetriNetRow> getSubscribedNets() throws InputTypeException {
        List<RecentNet> subscribedNets = ComputationsDAO.getRecentNetsSubscribedByUser(
                ViewNavigator.getAuthenticatedUser());
        List<PetriNetRow> netsToShow = new ArrayList<>();

        for (RecentNet net : subscribedNets) {
            if (!(net.getNet().getNetName() == null)){
                LocalDateTime date = determineNetDate(net);
                netsToShow.add(new PetriNetRow(
                        net.getNet().getNetName(),
                        net.getNet().getCreatorId(),
                        date,
                        Status.NOT_STARTED,
                        NetCategory.SUBSCRIBED
                ));
            }

        }
        return netsToShow;
    }

    /**
     * Gets owned nets for current user
     */
    private List<PetriNetRow> getOwnedNets() throws InputTypeException {
        List<RecentNet> ownNets = PetriNetsDAO.getNetsWithTimestampByCreator(
                ViewNavigator.getAuthenticatedUser());
        List<PetriNetRow> netsToShow = new ArrayList<>();

        for (RecentNet net : ownNets) {
            if (!(net.getNet().getNetName() == null)){
                LocalDateTime date = LocalDateTime.ofEpochSecond(net.getNet().getCreationDate(), 0, ZoneOffset.UTC);
                netsToShow.add(new PetriNetRow(
                        net.getNet().getNetName(),
                        net.getNet().getCreatorId(),
                        date,
                        determineNetStatus(net),
                        determineNetCategory(net)
                ));
            }
        }
        return netsToShow;
    }

    /**
     * Determines the appropriate date for a net (timestamp or creation date)
     */
    private LocalDateTime determineNetDate(RecentNet net) {
        if (net.getTimestamp() == null) {
            return LocalDateTime.ofEpochSecond(net.getNet().getCreationDate(), 0, ZoneOffset.UTC);
        } else {
            return LocalDateTime.ofEpochSecond(net.getTimestamp(), 0, ZoneOffset.UTC);
        }
    }

    /**
     * Determines the status of a net for current user
     */
    private Status determineNetStatus(RecentNet net) throws InputTypeException {
        String currentUsername = ViewNavigator.getAuthenticatedUser().getUsername();

        if(net.getComputation().getEndTimestamp()>0 & net.getTimestamp() == net.getComputation().getEndTimestamp()){
            return Status.COMPLETED;
        }
        else if(net.getComputation().getStartTimestamp()<0){
            return Status.NOT_STARTED;
        }
        else{
            if(net.getComputation().getNextStepType() == Computation.NEXT_STEP_TYPE.BOTH ||
                    (net.getComputation().getNextStepType() == Computation.NEXT_STEP_TYPE.ADMIN & ViewNavigator.getAuthenticatedUser().isAdmin())||
                    (net.getComputation().getNextStepType() == Computation.NEXT_STEP_TYPE.USER & !ViewNavigator.getAuthenticatedUser().isAdmin())){
                return Status.WAITING;
            }
            else{
                return Status.IN_PROGRESS;
            }
        }
    }

    /**
     * Determines the category of a net for current user
     */
    private NetCategory determineNetCategory(RecentNet net) throws InputTypeException {
        String currentUsername = ViewNavigator.getAuthenticatedUser().getUsername();

        if (net.getNet().getCreatorId().equals(currentUsername)) {
            return NetCategory.OWNED;
        }

        Computation computation = findUserComputation(net.getNet());
        return computation != null ? NetCategory.SUBSCRIBED : NetCategory.DISCOVER;
    }

//    /**
//     * Helper method to find computation for a PetriNet
//     */
//    private Computation findUserComputation(PetriNet net) throws InputTypeException {
//        return ComputationsDAO.getComputationsByNet(net.getNetName())
//                .stream()
//                .filter(computation -> computation.getUserId().equals(
//                        ViewNavigator.getAuthenticatedUser().getUsername()))
//                .findFirst()
//                .orElse(null);
//    }
}