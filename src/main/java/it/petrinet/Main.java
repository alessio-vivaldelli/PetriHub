package it.petrinet;

import it.petrinet.controller.MainController;
import it.petrinet.view.ViewNavigator;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.net.URL;

public class Main extends Application {
    private static Stage primaryStage;

    @Override
    public void start(Stage stage) throws Exception {
        primaryStage = stage;
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
        Parent root = loader.load();

        URL cssUrl = getClass().getResource("/styles/style.css");
        if (cssUrl != null) {
            String cssPath = cssUrl.toExternalForm();
            root.getStylesheets().add(cssPath);
        } else {
            System.err.println("CSS file not found: /styles/style.css");
        }


        Scene scene = new Scene(root, 500, 400);
        stage.setScene(scene);
        stage.setTitle("PH - Petri Nets Hub");
        stage.show();

        // inizializza ViewNavigator con controller
        MainController controller = loader.getController();
        ViewNavigator.init(controller);
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }
}
