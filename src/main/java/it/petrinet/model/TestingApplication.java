package it.petrinet.model;

import it.petrinet.petrinet.view.PetriNetEditorPane;
import it.petrinet.petrinet.view.PetriNetViewerPane;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class TestingApplication extends Application {

  private PetriNetEditorPane petriNetCreationPane;
  private PetriNetViewerPane petriNetViewerPane;

  @Override
  public void start(Stage stage) throws IOException {

     VBox vBox = createNet();
//    VBox vBox = visualizeNet();

    URL cssUrl = getClass().getResource("/styles/style.css");
    if (cssUrl != null) {
      String cssPath = cssUrl.toExternalForm();
      vBox.getStylesheets().add(cssPath);
    } else {
      System.err.println("CSS file not found: /styles/style.css");
    }
    Scene scene = new Scene(vBox, 1920, 1020);

    stage = new Stage(StageStyle.DECORATED);
    stage.setTitle("JavaFXGraph Visualization");
    stage.setScene(scene);
    stage.show();

     petriNetCreationPane.init();
//    petriNetViewerPane.init();
  }

  public VBox visualizeNet() {
    VBox vBox = new VBox();
    vBox.setSpacing(10);

    Computation computation = new Computation("testnet", "creatorID", "userID");
    computation.addStep(new ComputationStep(1, 1, "testnet", "", "start_e:1", 123456));
    // computation.addStep(new ComputationStep(2, 2, "testnet", "t1",
    // "p1:2,start_e:1,p2:1", 1234567));Computation

    String path = System.getProperty("user.dir") +
        "/src/main/resources/data/pnml/testing_petri_net.pnml";
    petriNetViewerPane = new PetriNetViewerPane(path, computation);

    vBox.getChildren().add(petriNetViewerPane);

    // Called when user/admin finish petri net
    petriNetViewerPane.setOnPetriNetFinished(() -> {
      System.out.println("Petri net finished");
    });

    // Called after a transition is fired
    //
    // @param String transitionName: clicked transition name
    // @param Map<String, Integer> newMarkingState: new marking state rappresented
    // as a map of <placeName, placeTockenCount> (only if count > 0)
    // @param List<Transition> transitions: list of new firable transitions list
    petriNetViewerPane.setOnTransitionFired((transitionName, newMarkingState, newTransition) -> {
      System.out.println("User click: " + transitionName + ", new marking state: " + newMarkingState
          + ", new firable transition: " + newTransition);
    });

    return vBox;
  }

  public VBox createNet() {
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

    return vBox;
  }

  // start application
  public static void main(String[] args) {
    launch();
  }
}
