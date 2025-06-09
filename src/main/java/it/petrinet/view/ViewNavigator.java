package it.petrinet.view;

import it.petrinet.Main;
import it.petrinet.controller.MainController;
import it.petrinet.controller.ShowAllController;
import it.petrinet.model.User;
import it.petrinet.view.components.NavBar;
import it.petrinet.view.components.NetCategory;
import it.petrinet.view.components.TableElement;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.util.Objects;

/**
 * Simple navigation utility for managing views and user authentication.
 */
public final class ViewNavigator {

    private static MainController mainController;
    private static User authenticatedUser;

    private ViewNavigator() {}

    public static void init(MainController controller) {
        mainController = Objects.requireNonNull(controller, "MainController cannot be null");
    }

    // Navigation methods
    public static void LoginScene() {
        mainController.setNavBar(null);
        resizeStage(500, 400, "PH - Login");
        loadView("LoginView.fxml");
    }

    public static void HomeScene() {
        mainController.setNavBar(new NavBar());
        resizeStage(0, 0, "Home");
        Main.getPrimaryStage().setTitle("PH - Petri Nets Hub");
        loadView("HomeView.fxml");
    }

    public static void navigateToLogin() {
        Main.getPrimaryStage().setTitle("PH - Login");
        loadView("LoginView.fxml");
    }

    public static void navigateToRegister() {
        Main.getPrimaryStage().setTitle("PH - Registration");
        loadView("RegisterView.fxml");
    }

    public static void navigateToMyNets() { navigateToShowAll(NetCategory.myNets);}

    public static void navigateToSubNets() { navigateToShowAll(NetCategory.mySubs);}

    //TODO: implement this method
    public static void navigateToDetail(TableElement net) {
        System.out.println("Navigating to detail view for net: " + net.getName());
    }

    public static void navigateToCreateNet() {
        System.out.println("Navigating to create net view");
    }
    //---------------------------

    public void navigateToDiscover() { navigateToShowAll(NetCategory.discover);}

    private static void navigateToShowAll(NetCategory type) {
        ShowAllController.setType(type);
        Main.getPrimaryStage().setTitle("All nets");
        loadView("ShowAllView.fxml");
    }

    public static void navigateToHome() {
        loadView("HomeView.fxml");
    }

    // User authentication
    public static void setAuthenticatedUser(User user) {
        authenticatedUser = user;
    }

    public static User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public static boolean userIsAdmin() {
        return authenticatedUser.isAdmin();
    }

    public static void logout() {
        authenticatedUser = null;
        navigateToLogin();
    }

    // Load FXML view
    private static void loadView(String fxmlName) {
        if (mainController == null) {
            throw new IllegalStateException("ViewNavigator not initialized. Call ViewNavigator.init() first.");
        }

        String path = "/fxml/" + fxmlName;
        URL resource = Main.class.getResource(path);
        if (resource == null) {
            throw new IllegalStateException("FXML resource not found: " + path);
        }

        try {
            FXMLLoader loader = new FXMLLoader(resource);
            Pane view = loader.load();
            mainController.setContent(view);
        } catch (IOException e) {
            System.err.println("Error loading view '" + fxmlName + "': " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Resize window with animation
    private static void resizeStage(double width, double height, String title) {
        Stage stage = Main.getPrimaryStage();
        if (stage == null) {
            throw new IllegalStateException("Primary stage is null");
        }

        // Calculate target size
        boolean maximize = (width == 0 && height == 0);
        if (maximize) {
            Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
            width = bounds.getWidth();
            height = bounds.getHeight();
        }

        final double targetWidth = width;
        final double targetHeight = height;
        final boolean shouldMaximize = maximize;

        // Fade out
        Timeline fadeOut = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(stage.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(250), new KeyValue(stage.opacityProperty(), 0.0))
        );

        // Pause
        PauseTransition pause = new PauseTransition(Duration.millis(100));

        // Fade in
        Timeline fadeIn = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(stage.opacityProperty(), 0.0)),
                new KeyFrame(Duration.millis(350), new KeyValue(stage.opacityProperty(), 1.0))
        );

        // Apply changes during pause
        pause.setOnFinished(e -> {
            stage.setTitle(title);
            stage.setWidth(targetWidth);
            stage.setHeight(targetHeight);

            if (shouldMaximize) {
                Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
                stage.setX(bounds.getMinX());
                stage.setY(bounds.getMinY());
            } else {
                // Center the window after resize
                Platform.runLater(() -> {
                    Rectangle2D screen = Screen.getPrimary().getVisualBounds();
                    double centerX = screen.getMinX() + (screen.getWidth() - stage.getWidth()) / 2;
                    double centerY = screen.getMinY() + (screen.getHeight() - stage.getHeight()) / 2;
                    stage.setX(centerX);
                    stage.setY(centerY);
                });
            }
        });

        // Play animation sequence
        new SequentialTransition(fadeOut, pause, fadeIn).play();
    }

}