package it.petrinet.model;

import atlantafx.base.theme.PrimerLight;
import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PLACE_TYPE;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.*;

import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.util.Random;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;

public class TestingApplication extends Application {

  private String currentNodeType = "transition"; // Default node type
  private String currentMode = "SELECTION"; // CREATE, CONNECT, SELECTION, or DELETION
  private Vertex<Node> firstSelectedVertex = null; // For connection mode

  @Override
  public void start(Stage stage) throws IOException {
    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    Graph<Node, String> g = new DigraphEdgeList<>();
    Node vElement = createNode("A", "transition");
    g.insertVertex(vElement);
    Node vElement_2 = createNode("B", "transition");
    g.insertVertex(vElement_2);
    Node vElement_3 = createNode("C", "circle");
    g.insertVertex(vElement_3);
    Node vElement_4 = createNode("D", "circle");
    g.insertVertex(vElement_4);

    g.insertEdge(vElement, vElement_2, "1");
    g.insertEdge(vElement_2, vElement_3, "2");
    g.insertEdge(vElement_3, vElement_4, "3");

    SmartPlacementStrategy initialPlacement = new SmartRandomPlacementStrategy();
    SmartGraphPanel<Node, String> graphView = new SmartGraphPanel<>(g, initialPlacement);

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
        String nodeLabel = currentNodeType.equals("circle") ? "New Place" : "New Transition";
        Vertex<Node> newVertex = g
            .insertVertex(createNode(nodeLabel, currentNodeType, new Point2D(point.getX(), point.getY())));
        graphView.updateAndWait();

        Vertex<Node> v = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == newVertex).findFirst().orElse(null);
        graphView.setVertexPosition(v, point.getX(), point.getY());

        graphView.updateAndWait();
        System.out.println("Created " + currentNodeType + " at pos " +
            graphView.getVertexPositionY(newVertex) + ", " +
            graphView.getVertexPositionX(newVertex));

        graphView.update();
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
            if (!newLabel.isEmpty() && !newLabel.equals(element.element().getName())) {
              element.element().setName(newLabel);
              System.out.println("Changed label to: " + newLabel);
              graphView.update();
            }
            contextMenu.hide();
          };

          labelField.setOnAction(_ -> applyLabelChange.run());
          // labelField.focusedProperty().addListener((obs, oldVal, newVal) -> {
          // if (!newVal) { // Focus lost
          // applyLabelChange.run();
          // }
          // });

          editBox.getChildren().addAll(editLabel, labelField);
          editLabelItem.setContent(editBox);
          editLabelItem.setHideOnClick(false); // Keep menu open while editing

          MenuItem deleteItem = new MenuItem("🗑️ Delete Node");
          deleteItem.setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
          deleteItem.setOnAction(_ -> {
            g.removeVertex(element);
            System.out.println("Deleted vertex: " + element.element().getName());
            graphView.update();
          });

          MenuItem changeTypeItem = new MenuItem();
          String currentType = element.element().getShapeType();
          if (currentType.equals("circle")) {
            changeTypeItem.setText("Change to Transition");
          } else {
            changeTypeItem.setText("Change to Place");
          }

          // Add user/admin toggle for transitions only
          MenuItem userTypeItem = null;
          if (currentType.equals("transition")) {
            userTypeItem = new MenuItem("Change to User"); // Default, since not implemented yet
            userTypeItem.setOnAction(_ -> {
              // TODO: Implement user/admin type change logic
              System.out.println("Changed transition to User type (not implemented yet)");
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
            contextMenu.getItems().addAll(editLabelItem, new SeparatorMenuItem(), changeTypeItem,
                userTypeItem, new SeparatorMenuItem(), infoItem, deleteItem);
          } else {
            contextMenu.getItems().addAll(editLabelItem, new SeparatorMenuItem(), changeTypeItem,
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

    graphView.setPrefHeight(700);
    ContentZoomScrollPane contentZoomScrollPane = new ContentZoomScrollPane(graphView);
    vBox.getChildren().addAll(contentZoomScrollPane, modeButtons, nodeTypeButtons);

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

  // Replace the CustomVertex usage with factory methods
  private Node createNode(String label, String type, Point2D position) {
    if (type.contains("transition")) {
      TRANSITION_TYPE transType = TRANSITION_TYPE.USER;
      return new Transition(label, position, transType);
    } else if (type.contains("place")) {
      return new Place(label, position, PLACE_TYPE.NORMAL, 0);
    }
    return new Place(label, position);
  }

  private Node createNode(String label, String type) {
    return createNode(label, type, new Point2D(0, 0));
  }

  // start application
  public static void main(String[] args) {
    launch();
  }
}
