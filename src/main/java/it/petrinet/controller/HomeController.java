package it.petrinet.controller;

import it.petrinet.model.User;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import it.petrinet.view.components.NetCategory;
import it.petrinet.view.components.PetriNetCard;
import it.petrinet.view.components.NewNetCard;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.io.InputStream;

public class HomeController {

    private User user; // Assicurati che la classe User esista e abbia un metodo getUsername() e isAdmin()

    @FXML
    private Label userNameLabel;

    @FXML
    private Hyperlink myNets; // Corrisponde al label "Creations" nel FXML
    @FXML
    private Hyperlink subNets; // Corrisponde al label "Subscriptions" nel FXML
    @FXML
    private Hyperlink discorver; // Corrisponde al label "Discover" nel FXML (attenzione al typo nel FXML se lo correggi)

    // Sezione MyNets (Creations)
    @FXML private HBox myNetsList;
    @FXML private VBox myNetsSection;
    @FXML private ScrollPane myNetsScrollPane;
    @FXML private Rectangle myNetsLeftShadow;
    @FXML private Rectangle myNetsRightShadow;
    @FXML private StackPane myNetsScrollContainer; // Non direttamente usato nel Java per le ombre, ma utile nel FXML

    // Sezione SubNets (Subscriptions)
    @FXML private HBox subNetsList;
    @FXML private VBox subNetsSection;
    @FXML private ScrollPane subNetsScrollPane;
    @FXML private Rectangle subNetsLeftShadow;
    @FXML private Rectangle subNetsRightShadow;
    @FXML private StackPane subNetsScrollContainer;

    // Sezione Discover
    @FXML private HBox discoverList;
    @FXML private VBox discoverSection;
    @FXML private ScrollPane discoverScrollPane;
    @FXML private Rectangle discoverLeftShadow;
    @FXML private Rectangle discoverRightShadow;
    @FXML private StackPane discoverScrollContainer;


    @FXML
    private void initialize() {
        // --- Inizializzazione Utente ---
        this.user = ViewNavigator.getAuthenticatedUser();
        if (user != null) {
            userNameLabel.setText("Welcome, " + user.getUsername());
        } else {
            userNameLabel.setText("Welcome, Guest!");
            // Se l'utente è null, puoi impostare un utente di default o reindirizzare
        }


        // --- Inizialmente Nascondi Sezioni e Pulisci Liste ---
        // Imposta setManaged(false) per non occupare spazio quando nascoste
        if (subNetsSection != null) {
            subNetsSection.setVisible(false);
            subNetsSection.setManaged(false);
        }
        if (myNetsSection != null) {
            myNetsSection.setVisible(false);
            myNetsSection.setManaged(false);
        }
        if (discoverSection != null) {
            discoverSection.setVisible(false);
            discoverSection.setManaged(false);
        }

        // Pulisce le liste (anche se sono vuote all'inizio)
        if (myNetsList != null) myNetsList.getChildren().clear();
        if (subNetsList != null) subNetsList.getChildren().clear();
        if (discoverList != null) discoverList.getChildren().clear();

        // Inizializzo icone hyperlinks per le sezioni
        IconUtils.setIcon(myNets, NetCategory.myNets.getDisplayName(), 24, 24, Color.PEACHPUFF);
        IconUtils.setIcon(subNets, NetCategory.mySubs.getDisplayName(), 24, 24, Color.PEACHPUFF);
        IconUtils.setIcon(discorver, NetCategory.discover.getDisplayName(), 24, 24, null);

        // --- Popola le Sezioni in base al Ruolo Utente ---
        if (user != null && user.isAdmin()) {
            setAllHomeSections();
        } else {
            setUserHome();
        }

        // --- Configura le Ombre di Scroll (DOPO che le sezioni sono state popolate e rese visibili) ---
        // Questo viene chiamato nel Platform.runLater all'interno di setupScrollShadows
        // per garantire che il layout sia stabile.
        // Controlla che gli elementi FXML non siano null prima di chiamare setupScrollShadows
        if (myNetsScrollPane != null) setupScrollShadows(myNetsScrollPane, myNetsLeftShadow, myNetsRightShadow);
        if (subNetsScrollPane != null) setupScrollShadows(subNetsScrollPane, subNetsLeftShadow, subNetsRightShadow);
        if (discoverScrollPane != null) setupScrollShadows(discoverScrollPane, discoverLeftShadow, discoverRightShadow);
    }


    /**
     * Configura la visibilità delle ombre di scroll per uno ScrollPane.
     * @param scrollPane Lo ScrollPane da monitorare.
     * @param leftShadow Il Rectangle che funge da ombra sinistra.
     * @param rightShadow Il Rectangle che funge da ombra destra.
     */
    private void setupScrollShadows(ScrollPane scrollPane, Rectangle leftShadow, Rectangle rightShadow) {
        if (scrollPane == null || leftShadow == null || rightShadow == null) {
            System.err.println("WARNING: ScrollPane or shadow Rectangles not injected for a section. Cannot setup shadows.");
            return;
        }

        // Listener per la proprietà hvalue (posizione di scroll orizzontale)
        scrollPane.hvalueProperty().addListener((obs, oldValue, newValue) -> {
            HBox contentHBox = (HBox) scrollPane.getContent();
            if (contentHBox == null) return; // Protezione se il contenuto non è ancora disponibile

            double contentWidth = contentHBox.getBoundsInLocal().getWidth();
            double viewportWidth = scrollPane.getWidth();

            // Calcola la scorrevolezza
            boolean isScrollable = (contentWidth > viewportWidth + 1.0); // +1.0 di tolleranza per floating point

            // Ombra sinistra: visibile se è scrollabile E non siamo all'inizio
            boolean showLeftShadow = isScrollable && newValue.doubleValue() > 0.001;

            // Ombra destra: visibile se è scrollabile E non siamo alla fine
            boolean showRightShadow = isScrollable && newValue.doubleValue() < 0.999;

            leftShadow.setVisible(showLeftShadow);
            leftShadow.setManaged(showLeftShadow);
            rightShadow.setVisible(showRightShadow);
            rightShadow.setManaged(showRightShadow);
        });

        // Ascolta i cambiamenti di dimensione dello ScrollPane (es. ridimensionamento finestra)
        scrollPane.widthProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                // Forza un ricalcolo delle ombre
                scrollPane.setHvalue(scrollPane.getHvalue());
            });
        });

        // Ascolta i cambiamenti di layout del contenuto (es. aggiunta/rimozione di card)
        scrollPane.getContent().layoutBoundsProperty().addListener((obs, oldVal, newVal) -> {
            Platform.runLater(() -> {
                // Forza un ricalcolo delle ombre
                scrollPane.setHvalue(scrollPane.getHvalue());
            });
        });

        // Forza un ricalcolo iniziale dopo che il layout è stabile
        Platform.runLater(() -> {
            scrollPane.setHvalue(scrollPane.getHvalue());
        });
    } //TODO: Fix this method


    private void setAllHomeSections() {
        // Rendi visibile e gestisci la sezione MyNets se l'utente è admin
        if (myNetsSection != null) {
            myNetsSection.setVisible(true);
            myNetsSection.setManaged(true);
        }
        populateMyNetList();
        populateSubNetList();
        populateDiscoverList();
    }

    private void setUserHome() {
        // L'utente non admin non ha la sezione "My Creations", quindi resta nascosta
        populateSubNetList();
        populateDiscoverList();
    }

    private void populateMyNetList() {
        if (user != null && user.isAdmin()) {
            if (myNetsList != null) {
                myNetsList.getChildren().clear();
                myNetsList.getChildren().add(new NewNetCard()); // Usa la tua classe NewNetCard
                // Aggiungi un numero sufficiente di card per abilitare lo scroll
                for (int i = 1; i <= 15; i++) { // Aumentato il numero di card per testare lo scroll
                    myNetsList.getChildren().add(new PetriNetCard("My Net " + i, "Description of Net " + i, ""));
                }
            }
        } else {
            // Se non è admin, assicurati che la sezione sia nascosta (già fatto in initialize, ma ridondanza sicura)
            if (myNetsSection != null) {
                myNetsSection.setVisible(false);
                myNetsSection.setManaged(false);
            }
            if (myNetsList != null) {
                myNetsList.getChildren().clear();
            }
        }
    }

    private void populateSubNetList() {
        // Rendi visibile e gestisci la sezione SubNets
        if (subNetsSection != null) {
            subNetsSection.setVisible(true);
            subNetsSection.setManaged(true);
        }
        if (subNetsList != null) {
            subNetsList.getChildren().clear();
            // Aggiungi un numero sufficiente di card per abilitare lo scroll
            for (int i = 1; i <= 12; i++) { // Aumentato
                subNetsList.getChildren().add(new PetriNetCard("Subscribed Net " + i, "Shared project from team " + i + ".", ""));
            }
        }
    }

    private void populateDiscoverList() {
        // Rendi visibile e gestisci la sezione Discover
        if (discoverSection != null) {
            discoverSection.setVisible(true);
            discoverSection.setManaged(true);
        }
        if (discoverList != null) {
            discoverList.getChildren().clear();
            // Aggiungi un numero sufficiente di card per abilitare lo scroll
            for (int i = 1; i <= 10; i++) { // Aumentato
                discoverList.getChildren().add(new PetriNetCard("Discover Net " + i, "Publicly available net " + i + ".", ""));
            }
        }
    }

    public void handleShowAllMyNets() { ViewNavigator.navigateToShowAll(NetCategory.myNets);}

    public void handleShowAllSubNets() { ViewNavigator.navigateToShowAll(NetCategory.mySubs);}

    public void handleShowAllDiscover() { ViewNavigator.navigateToShowAll(NetCategory.discover);}
}