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

import java.io.InputStream;
import java.util.Objects;

/**
 * A vertical navigation bar component for the left side of the application.
 * It extends VBox and includes a logo, navigation buttons, and a logout button
 * at the bottom.
 */
public class NavBar extends VBox {

    // Define the path to your logo. Update "logo.png" to your actual logo file name.
    private static final String pathToLogo = "/assets/images/logo.png";
    private static final String pathToIcon = "/assets/icons/";

    // Navigation buttons are created once
    private final Button homeButton = createNavButton("Home", ViewNavigator::navigateToHome, "home.png", "home");
    private final Button myNetsButton = createNavButton("My Nets", ViewNavigator::navigateToMyNets, "Creations.png", "myNets"); // Assuming an icon name
    private final Button subNetsButton = createNavButton("My Subs", ViewNavigator::navigateToSubNets, "Subscriptions.png", "subNets"); // Assuming an icon name
    private final Button discoverButton = createNavButton("Discover", this::handleDummy, "Discover.png", "discover"); // Placeholder for future functionality
    private final Button logoutButton = createNavButton("Logout", this::handleLogout, "logout.png", "logout");

    // Spacer to push the logout button to the bottom
    private final Region spacer = new Region();

    public NavBar() {
        setupLogo();
        setupLayout();

        // Populate the bar based on user role
        if (ViewNavigator.userIsAdmin()) {
            setAdminBar();
        } else {
            setUserBar();
        }
    }

    /**
     * Creates and adds the logo to the top of the NavBar.
     */
    private void setupLogo() {
        try {
            InputStream logoStream = getClass().getResourceAsStream(pathToLogo);
            if (logoStream == null) {
                System.err.println("Logo resource not found at: " + pathToLogo);
                // Handle error, maybe show a placeholder text
            } else {
                Image logo = new Image(logoStream);
                ImageView logoView = new ImageView(logo);
                logoView.setFitWidth(120); // Adjust width as needed
                logoView.setPreserveRatio(true);
                logoView.setSmooth(true);
                this.getChildren().add(logoView);
            }
        } catch (Exception e) {
            System.err.println("Error loading logo: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Configures the layout and style for the vertical navigation bar.
     */
    private void setupLayout() {
        this.setSpacing(15); // Space between vertical items
        this.setPadding(new Insets(20, 10, 20, 10)); // Top, Right, Bottom, Left padding
        this.getStyleClass().add("nav-bar-vertical");
        this.setAlignment(Pos.TOP_CENTER); // Align items to the top-center
        this.setMinWidth(150); // Set a minimum width for the bar

        // The spacer will grow vertically to push subsequent items down.
        VBox.setVgrow(spacer, Priority.ALWAYS);
    }

    /**
     * Populates the NavBar with buttons for a standard user.
     */
    private void setUserBar() {
        this.getChildren().addAll(
                homeButton,
                subNetsButton,
                discoverButton,
                spacer, // Pushes logout to the bottom
                logoutButton
        );
    }

    /**
     * Populates the NavBar with buttons for an admin user.
     */
    private void setAdminBar() {
        this.getChildren().addAll(
                homeButton,
                myNetsButton,
                subNetsButton,
                discoverButton,
                spacer, // Pushes logout to the bottom
                logoutButton
        );
    }

    /**
     * Creates a styled navigation button with an icon, text, and action.
     *
     * @param text     The text to display on the button.
     * @param action   The Runnable to execute on button click.
     * @param iconName The name of the icon file in the icon assets folder.
     * @param cssClass A specific CSS class for styling.
     * @return A configured Button.
     */
    private Button createNavButton(String text, Runnable action, String iconName, String cssClass) {
        Button button = new Button(text);
        button.setOnAction(e -> action.run());
        button.getStyleClass().add("nav-button-" + cssClass);
        button.setMaxWidth(Double.MAX_VALUE); // Allow button to fill the width of the VBox
        button.setAlignment(Pos.CENTER_LEFT); // Align text and icon to the left

        // Set icon using your utility class
        IconUtils.setIcon(button, iconName);

        return button;
    }

    /**
     * Placeholder method for handling navigation to project views.
     */
    private void handleDummy() {
        System.out.println("Navigating to projects (not yet implemented).");
        // Example: ViewNavigator.navigateToProjects();
    }

    private void handleLogout() {
        ViewNavigator.setAuthenticatedUser(null);
        ViewNavigator.LoginScene();
    }
}
