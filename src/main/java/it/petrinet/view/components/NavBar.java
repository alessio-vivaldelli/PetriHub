package it.petrinet.view.components;

import it.petrinet.view.ViewNavigator;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

import javax.swing.text.html.ImageView;
import java.io.InputStream;

public class NavBar extends HBox {

    private static final String pathToIcon = "/assets/icons/";

    private final Button homeButton = createNavButton("Home", ViewNavigator::navigateToHome, "home.png", "home");
    private final Button myNetsButton = createNavButton("My Nets", this::handleProjects, "myNets.png", "myNets");
    private final Button subNetsButton = createNavButton("My Subs", this::handleProjects, "subNets.png", "subNets");
    private final Button logoutButton = createNavButton("", this::handleLogout, "logout.png", "logout");

    HBox rightButton = new HBox();
    Region spacer = new Region();
    HBox leftButton = new HBox();

    public NavBar() {
        setupLayout();
        if (ViewNavigator.userIsAdmin())
            setAdminBar();
        else
            setUserBar();
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

        this.setFillHeight(true); // se VBox/HBox
        this.setPrefHeight(Region.USE_COMPUTED_SIZE);
        this.setMaxHeight(Region.USE_COMPUTED_SIZE);
        HBox.setHgrow(spacer, Priority.ALWAYS);
    }

    /** Crea un bottone con stile e azione */
    private static Button createNavButton(String text, Runnable action, String iconPath, String cssClass) {
        Button button = new Button(text);
        button.getStyleClass().add("navBar-" + cssClass);
        button.setOnAction(e -> action.run());

        if (iconPath != null && !iconPath.isBlank() && !iconPath.isEmpty()) {
            try (InputStream imageStream = NavBar.class.getResourceAsStream(pathToIcon + iconPath)) {
                if (imageStream != null) {
                    Image image = new Image(imageStream);
                    javafx.scene.image.ImageView icon = new javafx.scene.image.ImageView(image);
                    icon.setFitWidth(16);
                    icon.setFitHeight(16);
                    button.setGraphic(icon);
                } else {
                    System.err.println("Icon not found: " + iconPath);
                }
            } catch (Exception e) {
                System.err.println("Error loading icon: " + iconPath);
                e.printStackTrace();
            }
        }

        return button;
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
