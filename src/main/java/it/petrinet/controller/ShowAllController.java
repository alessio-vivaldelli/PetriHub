package it.petrinet.controller;

import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.model.TableRow.PetriNetRow;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.model.database.PetriNetsDAO;
import it.petrinet.service.SessionContext;
import it.petrinet.utils.IconUtils;
import it.petrinet.utils.NavigationHelper;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.table.PetriNetTableComponent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

import static it.petrinet.utils.Safenavigate.safeNavigate;
import static it.petrinet.utils.NetStatusGetter.determineNetDate;
import static it.petrinet.utils.NetStatusGetter.getStatusByComputation;

/**
 * Controller for displaying different categories of Petri nets in a table format.
 * Handles owned, subscribed, and discoverable nets with appropriate actions.
 */
public class ShowAllController {

    private static final Logger LOGGER = Logger.getLogger(ShowAllController.class.getName());
    private static final int ICON_SIZE = 30;

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;

    private PetriNetTableComponent petriNetTable;
    private NetCategory category;

    /**
     * Initializes the controller with the specified category and sets up the view.
     * This method should be called after FXML loading to provide the necessary data.
     *
     * @param category The category of nets to display (OWNED, SUBSCRIBED, or DISCOVER)
     */
    public void initData(NetCategory category) {
        this.category = Objects.requireNonNull(category, "Category cannot be null");

        try {
            initializeComponents();
            setupUserInterface();
            loadAndDisplayData();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize ShowAllController", e);
            // Could show error dialog here
        }
    }

    /**
     * Initializes the table component and adds it to the container.
     */
    private void initializeComponents() {
        petriNetTable = new PetriNetTableComponent();
        setupTableEventHandlers();

        VBox.setVgrow(petriNetTable, Priority.ALWAYS);
        tableContainer.getChildren().add(petriNetTable);
    }

    /**
     * Sets up the user interface based on the category.
     */
    private void setupUserInterface() {
        frameTitle.setText(" " + category.getDisplayName());
        IconUtils.setIcon(frameTitle, category.getDisplayName(), ICON_SIZE, ICON_SIZE, Color.WHITE);

        if (category == NetCategory.SUBSCRIBED) {
            petriNetTable.dataColSubName();
        }
    }

    /**
     * Sets up event handlers for table row clicks based on the category.
     */
    private void setupTableEventHandlers() {
        Function<PetriNetRow, Void> handler = (category == NetCategory.OWNED)
                ? this::handleOwnedNetClick
                : this::handleOtherNetClick;

        petriNetTable.setOnRowClickHandler(handler::apply);
    }

    /**
     * Handles clicks on owned nets - navigates to user list.
     */
    private Void handleOwnedNetClick(PetriNetRow row) {
        String netName = row.nameProperty().get();
        safeNavigate(() -> ViewNavigator.toComputationsList(netName));
        return null;
    }

    /**
     * Handles clicks on subscribed/discoverable nets.
     */
    private Void handleOtherNetClick(PetriNetRow row) {
        try {
            String netName = row.nameProperty().get();
            Optional<PetriNet> netOpt = Optional.ofNullable(PetriNetsDAO.getNetByName(netName));

            if (netOpt.isEmpty()) {
                LOGGER.warning("Net not found: " + netName);
                return null;
            }

            PetriNet net = netOpt.get();
            String username = SessionContext.getInstance().getUser().getUsername();

            switch (category) {
                case SUBSCRIBED -> NavigationHelper.setupNavigationToNetVisualForUser(net, username);
                case DISCOVER -> NavigationHelper.setupNavigationToTableDiscoverForUser(net, username);
                default -> LOGGER.warning("Unexpected category: " + category);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error handling table row click for category: " + category, e);
        }
        return null;
    }

    /**
     * Loads and displays data based on the current category.
     */
    private void loadAndDisplayData() {
        try {
            List<PetriNetRow> rows = loadDataForCategory();
            petriNetTable.setData(rows);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load data for category: " + category, e);
            petriNetTable.setData(Collections.emptyList());
        }
    }

    /**
     * Loads data based on the current category.
     */
    private List<PetriNetRow> loadDataForCategory() {
        return switch (category) {
            case OWNED -> createRowsFromNets(
                    PetriNetsDAO.getNetsByCreator(SessionContext.getInstance().getUser()),
                    this::getFirstSubscribedComputation
            );
            case SUBSCRIBED -> createRowsFromNets(
                    ComputationsDAO.getNetsSubscribedByUser(SessionContext.getInstance().getUser()),
                    this::findUserComputation
            );
            case DISCOVER -> createRowsFromNets(
                    PetriNetsDAO.getDiscoverableNetsByUser(SessionContext.getInstance().getUser()),
                    this::findUserComputation
            );
        };
    }

    /**
     * Creates table rows from a list of PetriNet objects.
     *
     * @param nets The list of PetriNet objects
     * @param computationProvider Function to get computation for each net
     * @return List of PetriNetRow objects for the table
     */
    private List<PetriNetRow> createRowsFromNets(List<PetriNet> nets, Function<PetriNet, Computation> computationProvider) {
        if (nets == null || nets.isEmpty()) {
            return Collections.emptyList();
        }

        return nets.stream()
                .filter(net -> net.getNetName() != null)
                .map(net -> createRowFromNet(net, computationProvider.apply(net)))
                .toList();
    }

    /**
     * Creates a PetriNetRow from a PetriNet and its computation.
     */
    private PetriNetRow createRowFromNet(PetriNet net, Computation computation) {
        return new PetriNetRow(
                net.getNetName(),
                net.getCreatorId(),
                determineNetDate(computation, net.getCreationDate()),
                getStatusByComputation(computation),
                category
        );
    }

    /**
     * Finds computation data for the current user and given net.
     */
    private Computation findUserComputation(PetriNet net) {
        try {
            return NavigationHelper.findUserComputation(net, SessionContext.getInstance().getUser().getUsername());
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to find user computation for net: " + net.getNetName(), e);
            return null;
        }
    }

    /**
     * Gets the first subscribed computation for a net (used for owned nets).
     */
    private Computation getFirstSubscribedComputation(PetriNet net) {
        try {
            ComputationStep step = ComputationStepDAO.getLastComputationStepForPetriNet(net.getNetName());
            return (step != null) ? ComputationStepDAO.getComputationByStep(step) : null;
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to get first subscribed computation for net: " + net.getNetName(), e);
            return null;
        }
    }
}