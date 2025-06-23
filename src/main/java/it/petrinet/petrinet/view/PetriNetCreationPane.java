package it.petrinet.petrinet.view;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;

import atlantafx.base.theme.PrimerLight;
import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.persistance.pnml.PNMLSerializer;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class PetriNetCreationPane extends Pane {

  public enum MODE {
    CREATE, CONNECT, SELECTION, DELETION
  }

  public enum NODE_TYPE {
    PLACE,
    TRANSITION,;

    @Override
    public String toString() {
      switch (this) {
        case PLACE:
          return "circle";
        case TRANSITION:
          return "transition";
        default:
          return super.toString();
      }
    }
  }

  private NODE_TYPE currentNodeType = NODE_TYPE.TRANSITION; // Default node type
  private MODE currentMode = MODE.CREATE; // CREATE, CONNECT, SELECTION, or DELETION
  private SmartGraphPanel<Node, String> graphView;
  private Vertex<Node> firstSelectedVertex = null; // For connection mode
  private Graph<Node, String> g; // The graph model
  private ContentZoomScrollPane contentZoomScrollPane;
  //
  //
  private String name = "TEST";
  private String description = "Test Petri Net";

  private PetriNetBuilder petriNetBuilder;

  private Consumer<String> onPetriNetSaved = null;

  private boolean firstStart = true; // To ensure the scene is initialized only IllegalConnectionException
  private boolean initialized;
  private boolean testMode;

  /**
   * Sets the callback to be invoked when a Petri Net is saved correctly.
   *
   * @param onPetriNetSaved Consumer that accepts the name of the saved Petri Net.
   */
  public void setOnPetriNetSaved(Consumer<String> onPetriNetSaved) {
    this.onPetriNetSaved = onPetriNetSaved;
  }

  /**
   * Sets the current mode of the Petri Net creation pane.
   * This method allows switching between different modes such as:
   * - CREATE: For creating new nodes.
   * - CONNECT: For connecting existing nodes.
   * - SELECTION: For selecting nodes and viewing their properties.
   * - DELETION: For deleting nodes or edges.
   * The mode affects how user interactions are handled within the pane.
   * For example, in CREATE mode, clicking on the canvas will create a new node,
   * while in CONNECT mode, it will allow connecting two existing nodes.
   *
   * @param mode The mode to set.
   */
  public void setCurrentMode(MODE mode) {
    this.currentMode = mode;
  }

  /**
   * Gets the current mode of the Petri Net creation pane.
   *
   * @return The current mode.
   */
  public MODE getCurrentMode() {
    return currentMode;
  }

  /**
   * Gets the current node type being used.
   *
   * @return The current node type.
   */
  public NODE_TYPE getCurrentNodeType() {
    return currentNodeType;
  }

  /**
   * Sets the current node type to be used when creating new nodes.
   * Possible types include:
   * - PLACE: Represents a place in the Petri Net.
   * - TRANSITION: Represents a transition in the Petri Net.
   *
   * @param currentNodeType The node type to set.
   */
  public void setCurrentNodeType(NODE_TYPE currentNodeType) {
    setCurrentMode(PetriNetCreationPane.MODE.CREATE);
    this.currentNodeType = currentNodeType;
  }

  /**
   * Handles the action of saving the Petri Net.
   * Shows a confirmation dialog, builds the model, serializes it, and invokes the
   * save callback if saved successfully and the callback is set.
   */
  public void saveNetAction() {
    showMessage(AlertType.CONFIRMATION, "Save Petri Net",
        "Save Confirmation", "Do you want to save the Petri Net?").ifPresent((response) -> {
          if (response.getButtonData() == ButtonBar.ButtonData.OK_DONE) {
            System.out.println("Saving Petri Net...");
          } else {
            System.out.println("Save cancelled.");
            return; // Exit if user cancels
          }
        });
    PetriNetModel model = null;
    try {
      prepareNodes(); // Ensure all nodes have their positions set
      model = petriNetBuilder.build();
      serializePetriNet(model);
      if (onPetriNetSaved != null)
        onPetriNetSaved.accept(model.getName());
    } catch (IllegalConnectionException e1) {
      System.out.println("Error building model");

      e1.printStackTrace();
      String errorMessage = e1.getMessage();
      showMessage(AlertType.ERROR, "Error", "Connection Error", errorMessage);
      System.out.println("Error building model: " + errorMessage);
    }
  }

  /**
   * When this method is called, it zooms in the canvas by a factor of 1.2.
   */
  public void zoomInAction() {
    System.out.println("Zoom in, current scale: " +
        contentZoomScrollPane.scaleFactorProperty().get());
    contentZoomScrollPane.zoomIn();
  }

  /**
   * When this method is called, it zooms out the canvas by a factor of 1.2.
   */
  public void zoomOutAction() {
    System.out.println("Zoom out, current scale: " +
        contentZoomScrollPane.scaleFactorProperty().get());
    contentZoomScrollPane.zoomOut();
  }

  /**
   * Gets the current zoom level of the Petri Net view.
   *
   * @return The current zoom level.
   */
  public double getZoomLevel() {
    return contentZoomScrollPane.scaleFactorProperty().get();
  }

  /**
   * Constructor for PetriNetCreationPane.
   * Initializes the pane with a petri name, description, and test mode flag.
   * Sets up the scene and starts the Petri Net creation process.
   * By default canvas will expand to fill all the available space.
   *
   * @param name        The name of the Petri Net.
   * @param description A description of the Petri Net.
   * @param testMode    If true, enables test mode with additional controls.
   */
  public PetriNetCreationPane(String name, String description, boolean testMode) {
    this.name = name;
    this.description = description;
    this.testMode = testMode;

    sceneProperty().addListener((_, _, newScene) -> {
      if (newScene != null) {
        newScene.windowProperty().addListener((_, _, newWindow) -> {
          if (newWindow != null && !initialized) {
            PauseTransition delay = new PauseTransition(Duration.millis(200));
            delay.setOnFinished(event -> {
              init();
            });
            delay.play();
          }
        });
      }
    });

    start();

    this.setPrefSize(1920, 1080);
  }

  public PetriNetCreationPane(String name, String description) {
    this(name, description, false);
  }

  public void start() {

    petriNetBuilder = new PetriNetBuilder(this.name);

    Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

    g = new DigraphEdgeList<>();
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
        currentMode = MODE.CREATE;
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Create");
      }
    });

    connectModeButton.setOnAction(e -> {
      if (connectModeButton.isSelected()) {
        currentMode = MODE.CONNECT;
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Connection");
      }
    });

    selectionModeButton.setOnAction(e -> {
      if (selectionModeButton.isSelected()) {
        currentMode = MODE.SELECTION;
        firstSelectedVertex = null; // Reset selection
        System.out.println("Mode: Selection");
      }
    });

    deletionModeButton.setOnAction(e -> {
      if (deletionModeButton.isSelected()) {
        currentMode = MODE.DELETION;
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
        currentNodeType = NODE_TYPE.PLACE;
        System.out.println("Node Type: Place");
      }
    });

    transitionButton.setOnAction(e -> {
      if (transitionButton.isSelected()) {
        currentNodeType = NODE_TYPE.TRANSITION;
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
      System.out.println("Canvas clicked at: " + point + ", Transformed: "
          + contentZoomScrollPane.transformFromContentToScaled(point));
      if (currentMode.equals(MODE.CREATE)) {
        // Create a new vertex at the clicked point using current node type
        // Prompt user for a unique node label using JavaFX
        String baseLabel = !currentNodeType.equals(NODE_TYPE.TRANSITION) ? "New Place" : "New Transition";
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
            .insertVertex(createNode(nodeLabel, currentNodeType.toString(),
                point));
        graphView.updateAndWait();

        Vertex<Node> v = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == newVertex).findFirst().orElse(null);

        if (currentNodeType.equals(NODE_TYPE.TRANSITION)) {
          graphView.getStylableVertex(v).setStyleClass("userTransition");
        }
        graphView.setVertexPosition(v, point.getX(), point.getY());

        graphView.updateAndWait();
        graphView.update();
      }
    });

    // Set up edge click action for deletion mode
    graphView.setEdgeSingleClickAction(edge -> {
      if (currentMode.equals(MODE.DELETION)) {
        // var element = graphView.getModel().edges().stream()
        // .filter(e -> e == edge.getUnderlyingEdge()).findFirst().orElse(null);
        var element = edge.getUnderlyingEdge();
        if (element != null) {
          petriNetBuilder.removeArc(edge.getUnderlyingEdge().vertices()[0].element().getName(),
              edge.getUnderlyingEdge().vertices()[1].element().getName());
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
      if (currentMode.equals(MODE.CREATE)) {
        Vertex<Node> element = graphView.getModel().vertices().stream()
            .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
        if (element != null) {
          removeNode(element);
          System.out.println("Deleted vertex: " + element.element().getName());
          graphView.update();
        }
        return; // Exit early if in CREATE mode
      }
      if (currentMode.equals(MODE.SELECTION)) {
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
              showMessage(AlertType.ERROR, "Invalid Label", "Label Change Error",
                  "The label must be unique and cannot be empty.");
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
            removeNode(element);
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
            showMessage(AlertType.INFORMATION, "Node Information", "Node Details",
                "Name: " + element.element().getName() +
                    "\nType: " + element.element().getShapeType() +
                    "\nVertex ID: " + element.toString());
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
      if (currentMode.equals(MODE.CONNECT)) {
        // Vertex<Node> element = graphView.getModel().vertices().stream()
        // .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
        Vertex<Node> element = vertex.getUnderlyingVertex();
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
                showMessage(AlertType.ERROR, "Connection Error",
                    "Incompatible Nodes",
                    "Cannot connect " + firstSelectedVertex.element().getName() +
                        " to " + element.element().getName() +
                        ". Only Place to Transition or Transition to Place connections are allowed.");
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
      } else if (currentMode.equals(MODE.SELECTION)) {
        // In SELECTION mode, show vertex info on left click
        System.out.println("Selected: " + vertex.getStylableLabel());
        System.out.println("------");
      } else if (currentMode.equals(MODE.DELETION)) {
        // In DELETION mode, delete the vertex on left click
        // Vertex<Node> element = graphView.getModel().vertices().stream()
        // .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
        Vertex<Node> element = vertex.getUnderlyingVertex();

        if (element != null) {
          removeNode(element);
          System.out.println("Deleted vertex: " + element.element().getName());
          graphView.update();
        }
      } else {
        // In CREATE mode, just show vertex info
        System.out.println("Click: " + vertex.getStylableLabel());
        System.out.println("------");
      }
    });

    contentZoomScrollPane = new ContentZoomScrollPane(graphView);

    graphView.setPrefHeight(700);
    HBox zoomControls = new HBox(10);
    Button zoomInButton = new Button("+");
    Button zoomOutButton = new Button("-");

    zoomInButton.setOnAction(_ -> {
      zoomInAction();
    });
    zoomOutButton.setOnAction(_ -> {
      zoomOutAction();
    });

    HBox actionControls = new HBox(10);
    Button saveNetButton = new Button("Save");
    saveNetButton.setOnAction(_ -> {
      saveNetAction();
    });

    actionControls.getChildren().addAll(saveNetButton);

    zoomControls.getChildren().addAll(zoomInButton, zoomOutButton);

    if (testMode) {
      vBox.getChildren().addAll(contentZoomScrollPane, modeButtons, nodeTypeButtons, zoomControls, actionControls);
    } else {
      vBox.getChildren().addAll(contentZoomScrollPane);
    }

    vBox.prefWidthProperty().bind(this.widthProperty());
    vBox.prefHeightProperty().bind(this.heightProperty());
    this.getChildren().add(vBox);
  }

  public void init() {

    // Initialize graph view
    graphView.init();
    // graphView.getStylableVertex(vElement_3).setStyleClass("svg_elem");

    graphView.setAutomaticLayout(true);
    graphView.setAutomaticLayout(false);

    graphView.update();
    contentZoomScrollPane.zoomIn();

  }

  private void createArc(String from, String to) {
    petriNetBuilder.addArc(from, to);
  }

  private void removeNode(Vertex<Node> node) {
    g.removeVertex(node);
    petriNetBuilder.removeNode(node.element().getName());
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

  private void serializePetriNet(PetriNetModel model) {
    System.out.println("Serializing Petri Net Model to PNML format...");
    PNMLSerializer serializer = new PNMLSerializer();
    try {
      serializer.serialize(model);
      System.out.println("Serialization complete.");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Optional<ButtonType> showMessage(AlertType type, String title, String headerText, String message) {
    Alert alert = new Alert(type);
    alert.setTitle("Error");
    alert.setHeaderText("An error occurred");
    alert.setContentText(message);
    return alert.showAndWait();
  }

  private void prepareNodes() {
    graphView.getModel().vertices().forEach(vtx -> {
      int x = (int) graphView.getVertexPositionX(vtx);
      int y = (int) graphView.getVertexPositionY(vtx);

      petriNetBuilder.setNodePosition(vtx.element().getName(), x, y);
    });
  }

}
