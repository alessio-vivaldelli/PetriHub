package it.petrinet.view.components;

import it.petrinet.utils.IconUtils;
import it.petrinet.view.ViewNavigator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple vertical navigation bar for the application sidebar.
 */
public class NavBar extends VBox {

    private static final Logger log = LoggerFactory.getLogger(NavBar.class);

    public NavBar() {
        setupLayout();
        setupLogo();
        setupButtons();
    }

    private void setupLayout() {
        setSpacing(15);
        setPadding(new Insets(20, 15, 20, 15));
        setAlignment(Pos.TOP_CENTER);
        setPrefWidth(250);
        getStyleClass().add("navBar");
    }

    private void setupLogo() {
        try {
            var logoStream = getClass().getResourceAsStream("/assets/images/logo.png");
            if (logoStream != null) {
                ImageView logo = new ImageView(new Image(logoStream));
                VBox logoSpacer = new VBox();
                logo.setFitWidth(180);
                logo.setPreserveRatio(true);
                logoSpacer.setStyle("-fx-padding: 10 0 20 15");
                logoSpacer.getChildren().add(logo);
                getChildren().add(logoSpacer);


                logoStream.close();
            }
        } catch (Exception e) {
            System.err.println("Could not load logo: " + e.getMessage());
        }
    }

    private void setupButtons() {
        // Create buttons
        Button homeBtn = createButton("Home", "home.png", "home", ViewNavigator::navigateToHome);
        Button subNetsBtn = createButton("My Subs", "Subscriptions.png", "subNets", ViewNavigator::navigateToSubNets);
        Button discoverBtn = createButton("Discover", "Discover.png", "discover", ViewNavigator::navigateToDiscover);

        // Spacer to push logout to bottom
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button logoutBtn = createButton("Logout", "logout.png", "logout", this::handleLogout);

        // Add buttons based on user role
        getChildren().addAll(homeBtn, subNetsBtn, discoverBtn);

        if (ViewNavigator.userIsAdmin()) {
            Button myNetsBtn = createButton("My Nets", "Creations.png", "myNets", ViewNavigator::navigateToMyNets);
            getChildren().add(2, myNetsBtn); // Insert after homeBtn
        }

        getChildren().addAll(spacer, logoutBtn);
    }

    private Button createButton(String text, String iconName, String cssClass, Runnable action) {
        Button button = new Button(" "+text);
        button.setOnAction(e -> action.run());
        button.getStyleClass().add("navBar-button-" + cssClass);
        button.setMaxWidth(Double.MAX_VALUE);
        button.setAlignment(Pos.CENTER_LEFT);
        IconUtils.setIcon(button, iconName);
        return button;
    }

    private void handleDiscover() {
        ViewNavigator.navigateToSubNets();
    }

    private void handleLogout() {
        ViewNavigator.setAuthenticatedUser(null);
        ViewNavigator.LoginScene();
    }
}