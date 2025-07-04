// src/main/java/it/petrinet/view/ShowAllController.java
package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.PetriNetRow;
import it.petrinet.model.TableRow.NetCategory;
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

import static it.petrinet.utils.ConstantPath.*;
import static it.petrinet.utils.Safenavigate.safeNavigate;

public class ShowAllController {

    @FXML private Label frameTitle;
    @FXML private VBox tableContainer;
    private PetriNetTableComponent petriNetTable;
    private static NetCategory cardType;

    public static void setType(NetCategory type) {
        cardType = type;
    }

    @FXML
    public void initialize() throws InputTypeException {
        frameTitle.setText(" "+cardType.getDisplayName());
        IconUtils.setIcon(frameTitle, cardType.getDisplayName(), 30, 30 , Color.WHITE);

        // Initialize the table component and load data
        initializeTableComponent();
        loadShowAllData();
    }

    private void initializeTableComponent() {
        petriNetTable = new PetriNetTableComponent();

        if( cardType == NetCategory.OWNED) {
            petriNetTable.setOnRowClickHandler(this::handleUserTableView);
        } else {
            petriNetTable.setOnRowClickHandler(e -> {
                try {
                    handleTableRowClick(e);
                } catch (InputTypeException ex) {
                    throw new RuntimeException(ex);
                }
            }); // Disable click handling for other categories
        }

        // Add the table component to the container
        VBox.setVgrow(petriNetTable, Priority.ALWAYS);
        tableContainer.getChildren().add(petriNetTable);
    }

    private void handleUserTableView(PetriNetRow petriNetRow) {
        safeNavigate(() -> ViewNavigator.navigateToUserList(petriNetRow.nameProperty().get()));
    }

    //Todo: aggiungere la navigazione a menagePetri
    private void handleTableRowClick(PetriNetRow petriNetRow) throws InputTypeException {
        PetriNet net =  PetriNetsDAO.getNetByName(petriNetRow.nameProperty().get());
        assert net != null;
        switch (cardType) {
            case SUBSCRIBED -> setupNavigation(net);
            case DISCOVER -> {}
            default -> throw new InputTypeException();
            }
        }

    private void setupNavigation(PetriNet net) throws InputTypeException {
        String path = netDirectory + net.getXML_PATH();
        Computation data =  ComputationsDAO.getComputationsByNet(net.getNetName())
                .stream()
                .filter(e -> e.getUserId().equals(ViewNavigator.getAuthenticatedUser().getUsername()))
                .findFirst().orElse(null);
        safeNavigate(() -> ViewNavigator.navigateToNetVisual(path,data));
    }


    private void loadShowAllData() throws InputTypeException {
        List<PetriNetRow> allNets = switch (cardType) {
            case OWNED -> OwnSet();
            case SUBSCRIBED -> SubSet();
            case DISCOVER -> DisSet();
            default -> null;
        };
        petriNetTable.setData(allNets);
    }

    private List<PetriNetRow> DisSet() throws InputTypeException {
        List<RecentNet> unknownNets = PetriNetsDAO.getUnknownNetsByUser(ViewNavigator.getAuthenticatedUser());

        List<PetriNetRow> netsToShow= new ArrayList<PetriNetRow>();
        for(RecentNet net : unknownNets){
            LocalDateTime creationDate = LocalDateTime.ofEpochSecond(net.getTimestamp(), 0, ZoneOffset.UTC);
            netsToShow.add(new PetriNetRow(net.getNet().getNetName(), net.getNet().getCreatorId(), creationDate, Status.NOT_STARTED, NetCategory.DISCOVER));
        }

        return netsToShow;
    }

    private List<PetriNetRow> SubSet() throws InputTypeException {
        List<RecentNet> subscribedNets = ComputationsDAO.getNetsSubscribedWithTimestampByUser(ViewNavigator.getAuthenticatedUser());

        List<PetriNetRow> netsToShow = new ArrayList<PetriNetRow>();
        for(RecentNet net : subscribedNets){
            LocalDateTime date;
            if(net.getTimestamp() == null){
                date = LocalDateTime.ofEpochSecond(net.getNet().getCreationDate(), 0, ZoneOffset.UTC);
            }
            else{
                date = LocalDateTime.ofEpochSecond(net.getTimestamp(), 0, ZoneOffset.UTC);
            }
            netsToShow.add(new PetriNetRow(net.getNet().getNetName(), net.getNet().getCreatorId(),date, Status.NOT_STARTED, NetCategory.SUBSCRIBED));
        }
        return netsToShow;
    }

    private List<PetriNetRow> OwnSet() throws InputTypeException {
        List<RecentNet> ownNets = PetriNetsDAO.getNetsWithTimestampByCreator(ViewNavigator.getAuthenticatedUser());

        List<PetriNetRow> netsToShow = new ArrayList<PetriNetRow>();
        for(RecentNet net : ownNets){
            LocalDateTime date;
            date = LocalDateTime.ofEpochSecond(net.getNet().getCreationDate(), 0, ZoneOffset.UTC);
            netsToShow.add(new PetriNetRow(net.getNet().getNetName(), net.getNet().getCreatorId(), date, getStatus(net), getCategory(net)));
        }

        return netsToShow;
    }

    private Status getStatus(RecentNet net) throws InputTypeException {
        boolean owend = net.getNet().getCreatorId().equals(ViewNavigator.getAuthenticatedUser().getUsername());

        if(!owend){
            Computation computation = ComputationsDAO.getComputationsByNet(net.getNet().getNetName())
                    .stream()
                    .filter(e -> e.getUserId().equals(ViewNavigator.getAuthenticatedUser().getUsername()))
                    .findFirst().orElse(null);
            assert computation != null;
            if(!computation.isStarted()) return Status.NOT_STARTED;
            if(computation.isFinished()) return Status.COMPLETED;

            boolean Noficiche = false;
            if(!Noficiche) return Status.IN_PROGRESS;
            else return Status.WAITING;

        }
        return Status.COMPLETED; // If the net is owned, it is considered completed
    }

    private NetCategory getCategory(RecentNet net) throws InputTypeException {
        if(net.getNet().getCreatorId().equals(ViewNavigator.getAuthenticatedUser().getUsername())) return NetCategory.OWNED;
        Computation computation = ComputationsDAO.getComputationsByNet(net.getNet().getNetName())
                .stream()
                .filter(e -> e.getUserId().equals(ViewNavigator.getAuthenticatedUser().getUsername()))
                .findFirst().orElse(null);
        if(computation != null) return NetCategory.SUBSCRIBED;
        else return NetCategory.DISCOVER;
    }

}
