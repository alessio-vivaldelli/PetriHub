package it.petrinet.view.components;

import it.petrinet.view.ViewNavigator;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.image.ImageView;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class NavBar extends HBox {

    private static final String pathToIcon = "/assets/icons/";
    // Soglia di larghezza per collassare i bottoni
    // Potrebbe essere necessario aggiustarla in base alla larghezza minima dei bottoni + margine
    private static final double COLLAPSE_WIDTH_THRESHOLD = 400; // Ridotta per un test più facile

    // Mappa per memorizzare il testo originale dei bottoni
    private final Map<Button, String> originalButtonTexts = new HashMap<>();

    private final Button homeButton = createNavButton("Home", ViewNavigator::navigateToHome, "home.png", "home");
    private final Button myNetsButton = createNavButton("My Nets", this::handleProjects, "myNets.png", "myNets");
    private final Button subNetsButton = createNavButton("My Subs", this::handleProjects, "subNets.png", "subNets");
    private final Button logoutButton = createNavButton("", this::handleLogout, "logout.png", "logout");

    HBox rightButton = new HBox();
    HBox leftButton = new HBox();
    Region spacer = new Region(); // Spacer per spingere i bottoni a destra

    public NavBar() {
        setupLayout();
        if (ViewNavigator.userIsAdmin())
            setAdminBar();
        else
            setUserBar();

        // Aggiungi listener per la larghezza della scena
        this.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null) {
                // Imposta un listener sulla larghezza della scena una volta che è disponibile
                newScene.widthProperty().addListener(new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observableValue, Number oldWidth, Number newWidth) {
                        updateButtonVisibility(newWidth.doubleValue());
                    }
                });
                // Chiamata iniziale per impostare lo stato corretto all'avvio
                updateButtonVisibility(newScene.getWidth());
            }
        });
    }

    private void setUserBar() {
        leftButton.getChildren().addAll(homeButton, subNetsButton);
        rightButton.getChildren().addAll(logoutButton);
        this.getChildren().addAll(leftButton, spacer, rightButton);
    }

    private void setAdminBar() {
        leftButton.getChildren().addAll(homeButton, myNetsButton, subNetsButton);
        rightButton.getChildren().addAll(logoutButton);
        this.getChildren().addAll(leftButton, spacer, rightButton);
    }

    /** Setup stile e padding */
    private void setupLayout() {
        this.setSpacing(10);
        this.setPadding(new Insets(10));
        this.getStyleClass().add("navBar");

        this.setFillHeight(true);
        this.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.setMaxHeight(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(spacer, Priority.ALWAYS); // Lo spacer prende lo spazio disponibile
    }

    /** Crea un bottone con stile e azione */
    private Button createNavButton(String text, Runnable action, String iconPath, String cssClass) {
        Button button = new Button(text);
        button.getStyleClass().add("navBar-" + cssClass);
        button.setOnAction(e -> action.run());

        // Memorizza il testo originale del bottone
        originalButtonTexts.put(button, text);

        ImageView icon = null;
        if (iconPath != null && !iconPath.isBlank()) {
            try (InputStream imageStream = NavBar.class.getResourceAsStream(pathToIcon + iconPath)) {
                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    icon = new ImageView(image);
                    icon.setFitWidth(24); // Aumentato leggermente per visibilità, puoi regolare
                    icon.setFitHeight(24); // Aumentato leggermente per visibilità, puoi regolare
                    button.setGraphic(icon);
                } else {
                    System.err.println("Icon not found: " + pathToIcon + iconPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading icon: " + pathToIcon + iconPath);
                e.printStackTrace();
            }
        }

        // Imposta una larghezza minima per il bottone.
        // Questo è cruciale per evitare che il bottone si rimpicciolisca troppo quando il testo scompare.
        // La larghezza dovrebbe essere almeno la larghezza dell'icona + padding orizzontale.
        // 24 (icona) + 10 (padding) = 34
        button.setMinWidth(40); // Puoi aggiustare questo valore

        return button;
    }

    /** Aggiorna la visibilità del testo dei bottoni in base alla larghezza della finestra */
    private void updateButtonVisibility(double stageWidth) {
//        if (stageWidth < COLLAPSE_WIDTH_THRESHOLD) {
//            // Rimuovi il testo
//            homeButton.setText("");
//            myNetsButton.setText("");
//            subNetsButton.setText("");
//            // Il bottone di logout non ha testo, quindi non ha bisogno di modifiche
//
//            // Per i bottoni che non hanno testo, potremmo voler aumentare il padding interno
//            // per far sembrare l'icona più centrata, o semplicemente lasciare il minWidth.
//            // Se imposti un padding orizzontale nel CSS, potresti non aver bisogno di questo.
//            homeButton.setPadding(new Insets(5)); // Esempio di padding ridotto
//            myNetsButton.setPadding(new Insets(5));
//            subNetsButton.setPadding(new Insets(5));
//
//
//        } else {
//            // Ripristina il testo originale
//            homeButton.setText(originalButtonTexts.get(homeButton));
//            myNetsButton.setText(originalButtonTexts.get(myNetsButton));
//            subNetsButton.setText(originalButtonTexts.get(subNetsButton));
//            // Ripristina il padding originale se lo hai modificato
//            homeButton.setPadding(new Insets(0, 10, 0, 10)); // Esempio di padding con testo (se non gestito da CSS)
//            myNetsButton.setPadding(new Insets(0, 10, 0, 10));
//            subNetsButton.setPadding(new Insets(0, 10, 0, 10));
//        }
    }

    /** Placeholder per progetti */
    private void handleProjects() {
        System.out.println("Progetti non ancora implementati");
        // ViewNavigator.navigateToProjects();
    }

    /** Azione di logout */
    private void handleLogout() {
        // reset utente autenticato
        ViewNavigator.setAuthenticatedUser(null);
        ViewNavigator.LoginScene();
    }
}