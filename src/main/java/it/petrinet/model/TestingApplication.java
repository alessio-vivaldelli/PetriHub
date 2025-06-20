package it.petrinet.model;

import java.io.IOException;

import it.petrinet.petrinet.view.PetriNetCreationPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TestingApplication extends Application {

  private PetriNetCreationPane petriNetCreationPane;

  @Override
  public void start(Stage stage) throws IOException {

    HBox vBox = new HBox();
    vBox.setSpacing(10);
    petriNetCreationPane = new PetriNetCreationPane("testNet", "description", true);
    vBox.getChildren().addAll(petriNetCreationPane);
    Scene scene = new Scene(vBox, 1920, 1020);

    stage = new Stage(StageStyle.DECORATED);
    stage.setTitle("JavaFXGraph Visualization");
    stage.setScene(scene);
    stage.show();

    // petriNetCreationPane.start();
    // petriNetCreationPane.init();
  }

  // start application
  public static void main(String[] args) {
    launch();
  }
}
