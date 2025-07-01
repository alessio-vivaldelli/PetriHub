// src/main/java/it/petrinet/view/ShowAllController.java
package it.petrinet.controller;

import it.petrinet.exceptions.InputTypeException;
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
        IconUtils.setIcon(frameTitle, cardType.getDisplayName() + ".png", 30, 30 , Color.WHITE);

        // Initialize the table component and load data
        initializeTableComponent();
        loadShowAllData();
    }

    private void initializeTableComponent() {
        petriNetTable = new PetriNetTableComponent();

        if( cardType == NetCategory.OWNED) {
            petriNetTable.setOnRowClickHandler(this::handleUserTableView);
        } else {
            petriNetTable.setOnRowClickHandler(this::handleTableRowClick); // Disable click handling for other categories
        }

        // Add the table component to the container
        VBox.setVgrow(petriNetTable, Priority.ALWAYS);
        tableContainer.getChildren().add(petriNetTable);
    }

    private void handleUserTableView(PetriNetRow petriNetRow) {
        ViewNavigator.navigateToUserList(petriNetRow.nameProperty().toString());
    }

    private void handleTableRowClick(PetriNetRow petriNetRow) {
        System.out.println("Row clicked: " + petriNetRow.nameProperty());
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
            netsToShow.add(new PetriNetRow(net.getNet().getNetName(), net.getNet().getCreatorId(), creationDate, Status.STARTED, NetCategory.DISCOVER));
        }

        return netsToShow;
    }

    private List<PetriNetRow> SubSet() throws InputTypeException {
        List<RecentNet> subscribedNets = ComputationsDAO.getNetsSubscribedWithTimestampByUser(ViewNavigator.getAuthenticatedUser());

        List<PetriNetRow> netsToShow = new ArrayList<PetriNetRow>();
        for(RecentNet net : subscribedNets){
            LocalDateTime date;
            if(net.getTimestamp()<0){
                date = LocalDateTime.ofEpochSecond(net.getNet().getCreationDate(), 0, ZoneOffset.UTC);
            }
            else{
                date = LocalDateTime.ofEpochSecond(net.getTimestamp(), 0, ZoneOffset.UTC);
            }
            netsToShow.add(new PetriNetRow(net.getNet().getNetName(), net.getNet().getCreatorId(),date, Status.STARTED, NetCategory.SUBSCRIBED));
        }
        return netsToShow;
    }

    private List<PetriNetRow> OwnSet() throws InputTypeException {
        List<PetriNet> ownNets = PetriNetsDAO.getNetsByCreator(ViewNavigator.getAuthenticatedUser());

        List<PetriNetRow> netsToShow = new ArrayList<PetriNetRow>();
        for(PetriNet net : ownNets){
            LocalDateTime creationDate = LocalDateTime.ofEpochSecond(net.getCreationDate(), 0, ZoneOffset.UTC);
            netsToShow.add(new PetriNetRow(net.getNetName(), net.getCreatorId(), creationDate, Status.STARTED, NetCategory.OWNED));
        }

        return netsToShow;
    }

}
