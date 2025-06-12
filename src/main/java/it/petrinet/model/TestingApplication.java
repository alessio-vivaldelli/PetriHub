package it.petrinet.model;

import atlantafx.base.theme.PrimerLight;
import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PLACE_TYPE;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;

import javafx.application.Application;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Optional;
import java.util.Random;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class TestingApplication extends Application {

  private String currentNodeType = "transition"; // Default node type
  private String currentMode = "CREATE"; // CREATE, CONNECT, SELECTION, or DELETION
  private SmartGraphPanel<Node, String> graphView;
  private Vertex<Node> firstSelectedVertex = null; // For connection mode
  //
  //
  private String name = "TEST";

  private PetriNetBuilder petriNetBuilder;

  public void setCurrentMode(String mode) {
    this.currentMode = mode;
  }

  @Override
  public void start(Stage stage) throws IOException {

    petriNetBuilder = new PetriNetBuilder(this.name);

    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    Graph<Node, String> g = new DigraphEdgeList<>();
    SmartPlacementStrategy initialPlacement = new SmartRandomPlacementStrategy();
    graphView = new SmartGraphPanel<>(g, initialPlacement);

    VBox vBox = new VBox();

    // Create toggle buttons for mode selection
    ToggleButton createModeButton = new ToggleButton("Create Mode");
    ToggleButton connectModeButton = new ToggleButton("Connection Mode");
    ToggleButton selectionModeButton = new ToggleButton("Selection Mode");
    ToggleButton deletionModeButton = new ToggleButton("Deletion Mode");

    ToggleGroup modeGroup = new ToggleGroup();
    createModeButton.setToggleGroup(modeGroup);
    connectModeButton.setToggleGroup(modeGroup);
    selectionModeButton.setToggleGroup(modeGroup);
    deletionModeButton.setToggleGroup(modeGroup);

    // Set default selection
    createModeButton.setSelected(true);

    // Handle mode toggle actions
    createModeButton.setOnAction(e -> {
      if (createModeButton.isSelected()) {
        currentMode = "CREATE";
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Create");
      }
    });

    connectModeButton.setOnAction(e -> {
      if (connectModeButton.isSelected()) {
        currentMode = "CONNECT";
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Connection");
      }
    });

    selectionModeButton.setOnAction(e -> {
      if (selectionModeButton.isSelected()) {
        currentMode = "SELECTION";
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Selection");
      }
    });

    deletionModeButton.setOnAction(e -> {
      if (deletionModeButton.isSelected()) {
        currentMode = "DELETION";
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Deletion");
      }
    });

    HBox modeButtons = new HBox(10);
    modeButtons.getChildren().addAll(createModeButton, connectModeButton, selectionModeButton, deletionModeButton);

    // Create toggle buttons for node type selection (only for create mode)
    ToggleButton placeButton = new ToggleButton("Place Mode");
    ToggleButton transitionButton = new ToggleButton("Transition Mode");

    ToggleGroup nodeTypeGroup = new ToggleGroup();
    placeButton.setToggleGroup(nodeTypeGroup);
    transitionButton.setToggleGroup(nodeTypeGroup);

    // Set default selection
    transitionButton.setSelected(true);

    // Handle toggle button actions
    placeButton.setOnAction(e -> {
      if (placeButton.isSelected()) {
        currentNodeType = "circle";
        System.out.println("Node Type: Place");
      }
    });

    transitionButton.setOnAction(e -> {
      if (transitionButton.isSelected()) {
        currentNodeType = "transition";
        System.out.println("Node Type: Transition");
      }
    });

    HBox nodeTypeButtons = new HBox(10);
    nodeTypeButtons.getChildren().addAll(placeButton, transitionButton);

    // // Randomize shapes button
    // Button randomizeButton = new Button("Randomize shapes");
    // randomizeButton.setOnMouseClicked(_ -> {
    // String[] shapeTypes = { "transition", "circle", "star", "svg" };
    // Random random = new Random();
    // for (Vertex<Node> vertex : graphView.getModel().vertices()) {
    // String shape = shapeTypes[random.nextInt(shapeTypes.length)];
    // vertex.element().setShapeType(shape);
    // }
    // graphView.update();
    // });

    // Set up canvas click action for creating new nodes (only in CREATE mode)
    graphView.setCanvasSingleClickAction(point -> {
      if (currentMode.equals("CREATE")) {
        // Create a new vertex at the clicked point using current node type
        // Prompt user for a unique node label using JavaFX
        String baseLabel = currentNodeType.equals("place") ? "New Place" : "New Transition";
        boolean unique = false;
        String nodeLabel = baseLabel;

        while (!unique) {
          TextInputDialog dialog = new TextInputDialog(nodeLabel);
          dialog.setTitle("New Node");
          dialog.setHeaderText("Enter a unique name for the new node:");
          dialog.setContentText("Name:");
          Optional<String> result = dialog.showAndWait();
          if (!result.isPresent() || result.get().trim().isEmpty()) {
            return; // Cancelled or empty input
          }
          String tmpNodeLabel = result.get().trim();
          nodeLabel = tmpNodeLabel;
          boolean exists = g.vertices().stream()
              .anyMatch(vtx -> vtx.element().getName().equals(tmpNodeLabel));
          if (!exists) {
            unique = true;
          } else {
            nodeLabel = tmpNodeLabel + " (copy)";
          }
        }
        Vertex<Node> newVertex = g
            .insertVertex(createNode(nodeLabel, currentNodeType, new Point2D(point.getX(), point.getY())));
        graphView.updateAndWait();

        Vertex<Node> v = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == newVertex).findFirst().orElse(null);

        if (currentNodeType.equals("transition")) {
          graphView.getStylableVertex(v).setStyleClass("userTransition");
        }
        graphView.setVertexPosition(v, point.getX(), point.getY());

        graphView.updateAndWait();
        graphView.update();
      } else {
        PetriNetModel model = null;
        try {
          model = petriNetBuilder.build();
        } catch (IllegalConnectionException e1) {
          e1.printStackTrace();
        }
        System.out.println("Model built: " + model);
      }
    });

    // Set up edge click action for deletion mode
    graphView.setEdgeSingleClickAction(edge -> {
      if (currentMode.equals("DELETION")) {
        var element = graphView.getModel().edges().stream()
            .filter(e -> e == edge.getUnderlyingEdge()).findFirst().orElse(null);
        if (element != null) {
          g.removeEdge(element);
          System.out.println("Deleted edge: " + element.element());
          graphView.update();
        }
      } else {
        System.out.println("Selected edge: " + edge);
      }
    });

    // Set up vertex right click action for selection mode
    graphView.setVertexRightClickAction(vertex -> {
      // If u are in create mode, u delete the vertex
      if (currentMode.equals("CREATE")) {
        Vertex<Node> element = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
        if (element != null) {
          g.removeVertex(element);
          System.out.println("Deleted vertex: " + element.element().getName());
          graphView.update();
        }
        return; // Exit early if in CREATE mode
      }
      if (currentMode.equals("SELECTION")) {
        Vertex<Node> element = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);

        if (element != null) {
          // Create context menu
          ContextMenu contextMenu = new ContextMenu();

          // Create custom MenuItem with TextField for label editing
          CustomMenuItem editLabelItem = new CustomMenuItem();
          VBox editBox = new VBox(5);
          Label editLabel = new Label("Edit Label:");
          TextField labelField = new TextField(element.element().getName());
          labelField.setPrefWidth(150);

          // Handle Enter key and focus lost to apply changes
          Runnable applyLabelChange = () -> {
            String newLabel = labelField.getText().trim();
            if (!newLabel.isEmpty() && !newLabel.equals(element.element().getName()) && isLabelUnique(newLabel)) {
              element.element().setName(newLabel);
              System.out.println("Changed label to: " + newLabel);
              graphView.update();
            } else {
              // If the label is not unique or empty, show an error message
              Alert alert = new Alert(Alert.AlertType.ERROR);
              alert.setTitle("Invalid Label");
              alert.setHeaderText("Label Change Error");
              alert.setContentText("The label must be unique and cannot be empty.");
              alert.showAndWait();

            }
            contextMenu.hide();
          };

          labelField.setOnAction(_ -> applyLabelChange.run());

          editBox.getChildren().addAll(editLabel, labelField);
          editLabelItem.setContent(editBox);
          editLabelItem.setHideOnClick(false); // Keep menu open while editing

          MenuItem deleteItem = new MenuItem("Delete Node");
          deleteItem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
          deleteItem.setOnAction(_ -> {
            g.removeVertex(element);
            System.out.println("Deleted vertex: " + element.element().getName());
            graphView.update();
          });

          String currentType = element.element().getShapeType();
          // Add user/admin toggle for transitions only
          MenuItem userTypeItem = null;
          MenuItem startPlaceItem = null;
          MenuItem endPlaceItem = null;
          if (currentType.equals("Transition")) {
            TRANSITION_TYPE current = ((Transition) element.element()).getType();
            userTypeItem = new MenuItem(
                (current.equals(TRANSITION_TYPE.ADMIN)) ? "Change to User"
                    : "Change to Admin"); // Default, since not implemented yet
            userTypeItem.setOnAction(_ -> {
              TRANSITION_TYPE newType = (current.equals(TRANSITION_TYPE.ADMIN)) ? TRANSITION_TYPE.USER
                  : TRANSITION_TYPE.ADMIN;
              ((Transition) element.element()).setType(newType);

              if (newType.equals(TRANSITION_TYPE.ADMIN)) {
                graphView.getStylableVertex(element).setStyleClass("adminTransition");
              } else {
                graphView.getStylableVertex(element).setStyleClass("userTransition");
              }
            });
          } else {
            startPlaceItem = new MenuItem("Set as Start Node");
            startPlaceItem.setOnAction(_ -> {

              if (petriNetBuilder.getStartNode() != null) {
                Vertex<Node> node = graphView.getModel().vertices().stream()
                    .filter(
                        vtx -> vtx.element() instanceof Place
                            && vtx.element().getName().equals(petriNetBuilder.getStartNode().getName()))
                    .findFirst().orElse(null);
                graphView.getStylableVertex(node).setStyleClass("vertex");

              }

              graphView.getStylableVertex(element).setStyleClass("startVertex");
              if (petriNetBuilder.getFinishNode() != null) {
                if (petriNetBuilder.getFinishNode().getName().equals(element.element().getName())) {
                  petriNetBuilder.setFinishNode(null);
                }
              }
              petriNetBuilder.setStartNode(element.element().getName());
            });
            endPlaceItem = new MenuItem("Set as Finish Node");
            endPlaceItem.setOnAction(_ -> {
              if (petriNetBuilder.getFinishNode() != null) {
                Vertex<Node> node = graphView.getModel().vertices().stream()
                    .filter(
                        vtx -> vtx.element() instanceof Place
                            && vtx.element().getName().equals(petriNetBuilder.getFinishNode().getName()))
                    .findFirst().orElse(null);
                graphView.getStylableVertex(node).setStyleClass("vertex");
              }
              if (petriNetBuilder.getStartNode() != null) {

                System.out.println("Old name: " + petriNetBuilder.getStartNode().getName() + " , New Name: "
                    + element.element().getName());
                if (petriNetBuilder.getStartNode().getName().equals(element.element().getName())) {
                  petriNetBuilder.setStartNode(null);
                }
              }
              graphView.getStylableVertex(element).setStyleClass("endVertex");
              petriNetBuilder.setFinishNode(element.element().getName());
            });
          }

          MenuItem infoItem = new MenuItem("Show Info");
          infoItem.setOnAction(_ -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Node Information");
            alert.setHeaderText("Node Details");
            alert.setContentText("Name: " + element.element().getName() +
                "\nType: " + element.element().getShapeType() +
                "\nVertex ID: " + element.toString());
            alert.showAndWait();
          });

          if (userTypeItem != null) {
            contextMenu.getItems().addAll(editLabelItem, new SeparatorMenuItem(),
                userTypeItem, new SeparatorMenuItem(), infoItem, deleteItem);
          } else {
            contextMenu.getItems().addAll(editLabelItem, new SeparatorMenuItem(), startPlaceItem, endPlaceItem,
                new SeparatorMenuItem(), infoItem, deleteItem);
          }

          // Show context menu at cursor position
          contextMenu.show(graphView, graphView.getScene().getWindow().getX() + graphView.getVertexPositionX(element),
              graphView.getScene().getWindow().getY() + graphView.getVertexPositionY(element));

          // Focus the text field after showing menu
          labelField.requestFocus();
          labelField.selectAll();
        }
      }
    });

    // Set up vertex click action for connections
    graphView.setVertexSingleClickAction(vertex -> {
      if (currentMode.equals("CONNECT")) {
        Vertex<Node> element = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
        if (firstSelectedVertex == null) {
          // First vertex selected
          firstSelectedVertex = element;
          System.out.println("First vertex selected: " + element.element().getName());
          // You could add visual feedback here (e.g., highlight the vertex)
        } else {
          // Second vertex selected, create connection
          if (firstSelectedVertex != element) {
            // Check if edge already exists
            boolean edgeExists = g.edges().stream()
                .anyMatch(edge -> (g.opposite(firstSelectedVertex, edge) == element) ||
                    (g.opposite(element, edge) == firstSelectedVertex));

            if (!edgeExists) {
              String edgeLabel = "edge_" + System.currentTimeMillis(); // Unique edge label
              // only connection with place -> transition or transition -> place are allowed
              if (!areCompatible(firstSelectedVertex.element(), element.element())) {
                System.out.println("Incompatible nodes for connection: " +
                    firstSelectedVertex.element().getName() + " and " + element.element().getName());
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Connection Error");
                alert.setHeaderText("Incompatible Nodes");
                alert.setContentText("Cannot connect " + firstSelectedVertex.element().getName() +
                    " to " + element.element().getName()
                    + ". Only Place to Transition or Transition to Place connections are allowed.");
                alert.showAndWait();
                firstSelectedVertex = null; // Reset selection
                return;
              }
              createArc(firstSelectedVertex.element().getName(), element.element().getName());
              g.insertEdge(firstSelectedVertex, element, edgeLabel);
              System.out.println("Connected " + firstSelectedVertex.element().getName() +
                  " to " + element.element().getName());
              graphView.update();
            } else {
              System.out.println("Edge already exists between " +
                  firstSelectedVertex.element().getName() + " and " +
                  element.element().getName());
            }
          } else {
            System.out.println("Cannot connect vertex to itself");
          }
          firstSelectedVertex = null; // Reset selection
        }
      } else if (currentMode.equals("SELECTION")) {
        // In SELECTION mode, show vertex info on left click
        System.out.println("Selected: " + vertex.getStylableLabel());
        System.out.println("------");
      } else if (currentMode.equals("DELETION")) {
        // In DELETION mode, delete the vertex on left click
        Vertex<Node> element = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
        if (element != null) {
          g.removeVertex(element);
          System.out.println("Deleted vertex: " + element.element().getName());
          graphView.update();
        }
      } else {
        // In CREATE mode, just show vertex info
        System.out.println("Click: " + vertex.getStylableLabel());
        System.out.println("------");
      }
    });

    ContentZoomScrollPane contentZoomScrollPane = new ContentZoomScrollPane(graphView);

    graphView.setPrefHeight(700);
    HBox zoomControls = new HBox(10);
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");

    zoomInButton.setOnAction(_ -> {
      System.out.println("Zoom in, current scale: " +
          contentZoomScrollPane.scaleFactorProperty().get());
      contentZoomScrollPane.zoomIn();
    });
    zoomOutButton.setOnAction(_ -> {
      System.out.println("Zoom out, current scale: " +
          contentZoomScrollPane.scaleFactorProperty().get());
      contentZoomScrollPane.zoomOut();
    });

    zoomControls.getChildren().addAll(zoomInButton, zoomOutButton);
    vBox.getChildren().addAll(contentZoomScrollPane, modeButtons, nodeTypeButtons, zoomControls);

    Scene scene = new Scene(vBox, 1024, 768);

    stage = new Stage(StageStyle.DECORATED);
    stage.setTitle("JavaFXGraph Visualization");
    stage.setScene(scene);
    stage.show();

    // Initialize graph view
    graphView.init();
    // graphView.getStylableVertex(vElement_3).setStyleClass("svg_elem");

    graphView.setAutomaticLayout(true);
    graphView.setAutomaticLayout(false);

    graphView.update();
  }

  private void createArc(String from, String to) {
    petriNetBuilder.addArc(from, to);
  }

  // Replace the CustomVertex usage with factory methods
  private Node createNode(String label, String type, Point2D position) {
    if (type.contains("transition")) {
      TRANSITION_TYPE transType = TRANSITION_TYPE.USER;
      Transition transition = new Transition(label, position, transType);
      PetriNetBuilder.TransitionBuilder transitioBuilder = petriNetBuilder.newTransition(transition);
      petriNetBuilder = transitioBuilder.doneTransition();
      System.out.println("Creating transition with label: " + label + " at position: " + position);
      return transition;
    } else if (type.contains("place") || type.contains("circle")) {
      System.out.println("Creating place with label: " + label + " at position: " + position);
      Place place = new Place(label, position);
      petriNetBuilder.newPlace(label).donePlace();
      return place;
    }
    return new Place(label, position);
  }

  private Node createNode(String label, String type) {
    return createNode(label, type, new Point2D(0, 0));
  }

  // check compatibility between two nodes
  private boolean areCompatible(Node node1, Node node2) {
    if (node1 instanceof Place && node2 instanceof Transition) {
      return true; // Place can connect to Transition
    } else if (node1 instanceof Transition && node2 instanceof Place) {
      return true; // Transition can connect to Place
    }
    return false; // Other combinations are not allowed
  }

  // Checks if the given label is unique among all elements in the graph
  private boolean isLabelUnique(String label) {
    return graphView.getModel().vertices().stream()
        .map(e -> e.element().getName())
        .noneMatch(name -> name.equals(label));
  }

  // start application
  public static void main(String[] args) {
    launch();
  }
}
