package it.petrinet.view;

import it.petrinet.Main;
import it.petrinet.controller.*;
import it.petrinet.model.Computation;
import it.petrinet.model.PetriNet;
import it.petrinet.model.TableRow.NetCategory;
import it.petrinet.model.User;
import it.petrinet.view.components.NavBar;
import it.petrinet.controller.NetVisualController.VisualState;
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
import java.util.function.Consumer;

/**
 * REFACTOR: A Facade and Mediator for view navigation.
 * This class is a static utility that provides a single point of control for switching scenes,
 * passing data to controllers safely without using static controller state.
 */
public final class ViewNavigator {

    private static final String FXML_RESOURCE_PATH = "/fxml/";

    private static MainController mainController;
    private static User authenticatedUser;
    private static String pendingMessage;
    private static boolean isAdmin;

    private ViewNavigator() {}

    public static void init(MainController controller) {
        mainController = Objects.requireNonNull(controller, "MainController cannot be null");
    }

    // =================================================================================
    // PRIMARY NAVIGATION SCENES
    // =================================================================================

    public static void loginScene() {
        mainController.setNavBar(null);
        resizeStage(500, 400, "PH - Petri Nets Hub");
        authenticatedUser = null; // Reset authenticated user
        loadView("LoginView.fxml");
    }

    public static void homeScene() {
        mainController.setNavBar(new NavBar());
        resizeStage(0, 0, "PH - Home");
        loadView("HomeView.fxml");
    }

    public static void exitPetriNetScene() {
        mainController.setNavBar(new NavBar());
        homeScene();
    }

    // =================================================================================
    // SECONDARY NAVIGATION METHODS
    // =================================================================================

    public static void toLogin() {
        loadView("LoginView.fxml");
    }

    public static void toRegister() {
        loadView("RegisterView.fxml");
    }

    public static void toHome(){
        mainController.setNavBar(new NavBar());
        loadView("HomeView.fxml");
    }

    public static void toMyNets() {
        navigateToShowAll(NetCategory.OWNED);
    }

    public static void toSubscribedNets() {
        navigateToShowAll(NetCategory.SUBSCRIBED);
    }

    public static void toDiscoverNets() {
        navigateToShowAll(NetCategory.DISCOVER);
    }

    public static void toUserList(String netId) {
        // REFACTOR: Use the new initializer pattern
        loadView("ComputationListView.fxml", ComputationListController.class, controller -> controller.initData(netId));
    }

    public static void toNetCreation(String netName) {
        mainController.setNavBar(null);
        // REFACTOR: Use the new initializer pattern
        loadView("NetCreationView.fxml", NetCreationController.class, controller -> controller.initData(netName));
    }

    public static void toNetVisual(PetriNet model, Computation data, VisualState state) {
        mainController.setNavBar(null);
        // REFACTOR: Use the new initializer pattern
        loadView("NetVisualView.fxml", NetVisualController.class, controller -> controller.initData(model, data, state));
    }

    private static void navigateToShowAll(NetCategory type) {
        // REFACTOR: Use the new initializer pattern
        loadView("ShowAllView.fxml", ShowAllController.class, controller -> controller.initData(type));
    }

    // =================================================================================
    // CORE VIEW LOADING LOGIC
    // =================================================================================

    /**
     * REFACTOR: Overloaded helper for loading views that don't need initial data.
     */
    private static void loadView(String fxmlName) {
        loadView(fxmlName, Object.class, controller -> {});
    }

    /**
     * REFACTOR: This is the new core method for loading views. It's generic and type-safe.
     * It loads the FXML, gets the controller instance, sets the content, and then runs the
     * provided initializer lambda on the controller after the view is loaded.
     *
     * @param fxmlName The name of the FXML file.
     * @param controllerClass The class of the controller (for type safety).
     * @param initializer A lambda function to run on the controller instance after it's loaded.
     */
    private static <T> void loadView(String fxmlName, Class<T> controllerClass, Consumer<T> initializer) {
        if (mainController == null) {
            throw new IllegalStateException("ViewNavigator not initialized. Call ViewNavigator.init() first.");
        }

        try {
            String path = FXML_RESOURCE_PATH + fxmlName;
            URL resource = Main.class.getResource(path);
            if (resource == null) {
                throw new IOException("FXML resource not found: " + path);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Pane view = loader.load();

            // Get the controller instance
            T controller = loader.getController();
            if (controller == null) {
                throw new IllegalStateException("Controller not found for FXML: " + fxmlName);
            }

            // Set the content first
            mainController.setContent(view);

            // CRITICAL FIX: Run initializer on the JavaFX Application Thread after the view is displayed
            Platform.runLater(() -> {
                try {
                    initializer.accept(controller);
                } catch (Exception e) {
                    System.err.println("Error initializing controller for '" + fxmlName + "': " + e.getMessage());
                    e.printStackTrace();
                }
            });

        } catch (IOException e) {
            System.err.println("Error loading view '" + fxmlName + "': " + e.getMessage());
            e.printStackTrace();
            // Optionally, show an error alert to the user
        }
    }

    // =================================================================================
    // SESSION AND MESSAGE MANAGEMENT
    // =================================================================================

    public static void setAuthenticatedUser(User user) {
        authenticatedUser = user;
        isAdmin = user != null && user.isAdmin();
    }

    public static User getAuthenticatedUser() {
        return authenticatedUser;
    }

    public static boolean userIsAdmin() {
        return isAdmin;
    }

    public static void toLoginWithMessage(String message) {
        pendingMessage = message;
        toLogin();
    }

    public static String consumePendingMessage() {
        String message = pendingMessage;
        pendingMessage = null;
        return message;
    }

    // =================================================================================
    // STAGE AND ANIMATION MANAGEMENT
    // =================================================================================

    private static void resizeStage(double width, double height, String title) {
        Stage stage = Main.getPrimaryStage();
        stage.setMaximized(false);

        final boolean maximize = (width == 0 && height == 0);
        final double targetWidth = maximize ? Screen.getPrimary().getVisualBounds().getWidth() : width;
        final double targetHeight = maximize ? Screen.getPrimary().getVisualBounds().getHeight() : height;

        Timeline fadeOut = new Timeline(new KeyFrame(Duration.millis(200), new KeyValue(stage.opacityProperty(), 0.0)));
        PauseTransition pause = new PauseTransition(Duration.millis(50));
        Timeline fadeIn = new Timeline(new KeyFrame(Duration.millis(300), new KeyValue(stage.opacityProperty(), 1.0)));

        pause.setOnFinished(e -> {
            stage.setWidth(targetWidth);
            stage.setHeight(targetHeight);
            stage.setTitle(title);

            if (maximize) {
                stage.setMaximized(true);
            } else {
                // Center the window
                Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();
                stage.setX((screenBounds.getWidth() - targetWidth) / 2);
                stage.setY((screenBounds.getHeight() - targetHeight) / 2);
            }
        });

        new SequentialTransition(fadeOut, pause, fadeIn).play();
    }
}