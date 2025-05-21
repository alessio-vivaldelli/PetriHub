package it.petrinet.view;

import it.petrinet.Main;
import it.petrinet.controller.MainController;
import it.petrinet.model.User;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import java.io.IOException;
import java.net.URL;

// Casino totale qua, dobbiamo vederi per sistemare !!!!

/**
 * This class handles navigation between different views in the application.
 * It works as a bridge between controllers and views, allowing for simplified navigation.
 */
public class ViewNavigator {
    // Reference to the main controller
    private static MainController mainController;

    // Current authenticated username
    private static User authenticatedUser = null;  // not null if logged in

  /*  public static void setSelectedProject(Project project) {
        selectedProject = project;
    }
    magari può servire per le petri net

    public static Project getSelectedProject() {
        return selectedProject;
    }
    anche quest
  */


    /**
     * Set the main controller reference
     * @param controller The MainController instance
     */
    public static void setMainController(MainController controller) {
        mainController = controller;
    }

    /**
     * Load and switch to a view
     * @param fxml The name of the FXML file to load
     */
    public static void loadView(String fxml) {
        try {
            URL fxmlUrl = Main.class.getResource("/fxml/" + fxml);
            FXMLLoader loader = new FXMLLoader(fxmlUrl);
            Node view = loader.load();
            mainController.setContent(view);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading view: " + fxml);
        }
    }



    /**
     * Navigate to the home view
     */
//    public static void navigateToHome() {
//        loadView("HomeView.fxml");
//    }

    /**
     * Navigate to the login view
     */
    public static void navigateToLogin() {
        loadView("LoginView.fxml");

    }

    /**
     * Navigate to the register view
     */
    public static void navigateToRegister() {
        loadView("RegisterView.fxml");
    }

    /**
     * Set the authenticated user
     * @param username The username of the authenticated user
     */
    public static void setAuthenticatedUser(User user) {
        authenticatedUser = user;
    }

    /**
     * Get the authenticated user
     * @return The username of the authenticated user, or null if not authenticated
     */
    public static User getAuthenticatedUser() {
        return authenticatedUser;
    }

    /**
     * Check if a user is authenticated
     * @return true if a user is authenticated, false otherwise
     */
    public static boolean isAuthenticated() {
        return authenticatedUser != null;
    }

    /**
     * Logout the current user
     */
    public static void logout() {
        System.out.println("Logout fatto, ho appena fatto la caccona nel puzzone, spero non si veda nella cronologia di git <3");
    }
}