package it.petrinet.model;

import java.io.IOException;

import it.petrinet.petrinet.view.PetriNetEditorPane;
import it.petrinet.petrinet.view.PetriNetEditorPane;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class TestingApplication extends Application {

  private PetriNetEditorPane petriNetCreationPane;

  @Override
  public void start(Stage stage) throws IOException {

    VBox vBox = new VBox();
    vBox.setSpacing(10);

    // Create buttons for different actions
    Button creation = new Button("Create");
    Button connect = new Button("Connect");
    Button delete = new Button("Delete");
    Button save = new Button("Save");
    Button select = new Button("Select");

    HBox buttonBox = new HBox();
    buttonBox.setSpacing(10);
    buttonBox.getChildren().addAll(creation, connect, delete, save, select);

    // Initialize the PetriNetEditorPane with a name and description
    petriNetCreationPane = new PetriNetEditorPane();

    // Set the initial mode to Select
    petriNetCreationPane.setCurrentMode(PetriNetEditorPane.MODE.SELECTION);

    // Set the button actions to change the mode of the PetriNetEditorPane
    // creation.setOnAction(_ -> {
    // petriNetCreationPane.setCurrentMode(PetriNetEditorPane.MODE.CREATE);
    // });
    connect.setOnAction(_ -> {
      petriNetCreationPane.setCurrentMode(PetriNetEditorPane.MODE.CONNECT);
    });
    delete.setOnAction(_ -> {
      petriNetCreationPane.setCurrentMode(PetriNetEditorPane.MODE.DELETION);
    });
    save.setOnAction(_ -> {
      petriNetCreationPane.saveNetAction("testing_petri_net", "description");
    });
    select.setOnAction(_ -> {
      petriNetCreationPane.setCurrentMode(PetriNetEditorPane.MODE.SELECTION);
    });

    // Set the select button to change the node type to create a new node
    Button place = new Button("Place");
    Button transition = new Button("Transition");
    place.setOnAction(_ -> {
      petriNetCreationPane.setCurrentNodeType(PetriNetEditorPane.NODE_TYPE.PLACE);
    });
    transition.setOnAction(_ -> {
      petriNetCreationPane.setCurrentNodeType(PetriNetEditorPane.NODE_TYPE.TRANSITION);
    });
    HBox nodeTypeBox = new HBox();
    nodeTypeBox.setSpacing(10);
    nodeTypeBox.getChildren().addAll(place, transition);

    // Create zoom in and zoom out buttons and add handlers for zoom actions
    Button zoomIn = new Button("Zoom In");
    Button zoomOut = new Button("Zoom Out");
    zoomIn.setOnAction(_ -> {
      petriNetCreationPane.zoomInAction();
    });
    zoomOut.setOnAction(_ -> {
      petriNetCreationPane.zoomOutAction();
    });
    HBox zoomBox = new HBox();
    zoomBox.setSpacing(10);
    zoomBox.getChildren().addAll(zoomIn, zoomOut);

    vBox.getChildren().addAll(petriNetCreationPane, buttonBox, nodeTypeBox, zoomBox);

    Scene scene = new Scene(vBox, 1920, 1020);

    stage = new Stage(StageStyle.DECORATED);
    stage.setTitle("JavaFXGraph Visualization");
    stage.setScene(scene);
    stage.show();

    petriNetCreationPane.init();
  }

  // start application
  public static void main(String[] args) {
    launch();
  }
}
