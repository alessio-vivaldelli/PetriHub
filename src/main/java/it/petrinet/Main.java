package it.petrinet;

import it.petrinet.model.User;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;


import java.net.URL;
import java.util.List;

public class Main extends Application {
    public void start(Stage primaryStage) throws Exception {  // Inizializza il main view app
        // Load the main application view
        URL mainViewUrl = getClass().getResource("/fxml/MainView.fxml");
        FXMLLoader loader = new FXMLLoader(mainViewUrl);

        Parent root = loader.load();

        // Set up the scene
        Scene scene = new Scene(root, 800, 600);
        String cssPath = "/styles/style.css";
        URL cssUrl = getClass().getResource(cssPath);
        if (cssUrl != null) {
            scene.getStylesheets().add(cssUrl.toExternalForm());
        } else {
            System.err.println("Impossibile trovare il foglio di stile: " + cssPath);
        }

        // Configure and show the stage
        primaryStage.setTitle("PH - Petri Nets Hub");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    //non so se dovrebbe stare qua
    public static boolean isValidInput(String username, String password) {
        String safePattern = "^[a-zA-Z0-9_\\-.@]+$";
        return username != null && !username.isEmpty() && password != null && !password.isEmpty() &&
                username.matches(safePattern) && password.matches(safePattern);
    }
}
