package it.petrinet.petrinet.view;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphEdge;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.*;
import it.petrinet.petrinet.persistance.pnml.PNMLSerializer;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.components.EnhancedAlert;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

public class PetriNetEditorPane extends AbstractPetriNetPane {
  public enum MODE {
    CREATE, CONNECT, SELECTION, DELETION
  }

  public enum NODE_TYPE {
    PLACE,
    TRANSITION,;

    @Override
    public String toString() {
      return switch (this) {
        case PLACE -> "circle";
        case TRANSITION -> "transition";
        default -> super.toString();
      };
    }
  }

  private NODE_TYPE currentNodeType = NODE_TYPE.TRANSITION;
  private MODE currentMode = MODE.CREATE;

  private String name = "";
  private String description = "";

  private PetriNetBuilder petriNetBuilder;
  private Consumer<String> onPetriNetSaved = null;

  private boolean firstStart = true;
  private Vertex<Node> firstSelectedVertex = null; // For connection mode

  public PetriNetEditorPane(String petriNetName, String petriNetDescription) {
    super();
    this.name = petriNetName;
    this.description = petriNetDescription;
    petriNetBuilder = new PetriNetBuilder(this.name);
  }

  public PetriNetEditorPane(String petriNetName) {
    this(petriNetName, "");
  }

  public PetriNetEditorPane() {
    this("", "");
  }

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
    setCurrentMode(MODE.CREATE);
    this.currentNodeType = currentNodeType;
  }

  public void saveNetAction() {
    saveNetAction(name, description);
  }

  /**
   * Handles the action of saving the Petri Net.
   * Shows a confirmation dialog, builds the model, serializes it, and invokes the
   * save callback if saved successfully and the callback is set.
   */
  public void saveNetAction(String petriNetName, String petriNetDescription) {
    this.name = petriNetName;
    this.description = petriNetDescription;

    if (petriNetName.isEmpty()) {
      EnhancedAlert.showError("Error", "Please provide a valid name for the Petri Net."); // Changed
      return; // Exit if name is empty
    }

    petriNetBuilder.setPetriName(name);

    Optional<EnhancedAlert.AlertResult> result = EnhancedAlert.showConfirmation( // Changed
            "Save Petri Net", "Do you want to save the Petri Net?");

    if (result.isPresent() && result.get().isYes()) {
      System.out.println("Saving Petri Net...");
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
        EnhancedAlert.showError("Connection Error", errorMessage); // Changed
        System.out.println("Error building model: " + errorMessage);
      }
    } else {
      System.out.println("Save cancelled.");
    }
  }

  public String getName() {
    return name;
  }

  public String getDescription() {
    return description;
  }

  public PetriNetBuilder getPetriNetBuilder() {
    return petriNetBuilder;
  }

  public void setPetriNetBuilder(PetriNetBuilder petriNetBuilder) {
    this.petriNetBuilder = petriNetBuilder;
  }

  public Consumer<String> getOnPetriNetSaved() {
    return onPetriNetSaved;
  }

  public boolean isFirstStart() {
    return firstStart;
  }

  public void setFirstStart(boolean firstStart) {
    this.firstStart = firstStart;
  }

  public Vertex<Node> getFirstSelectedVertex() {
    return firstSelectedVertex;
  }

  public void setFirstSelectedVertex(Vertex<Node> firstSelectedVertex) {
    this.firstSelectedVertex = firstSelectedVertex;
  }

  @Override
  protected void onGraphInitialized() {
    super.onGraphInitialized();
  }

  @Override
  protected void onCanvasSingleClickAction(Point2D point) {
    super.onCanvasSingleClickAction(point);

    if (currentMode.equals(MODE.CREATE)) {
      String baseLabel = !currentNodeType.equals(NODE_TYPE.TRANSITION) ? "New Place" : "New Transition";
      boolean unique = false;
      String nodeLabel = baseLabel;

      // Loop to ensure unique label
      while (!unique) {
        Optional<EnhancedAlert.AlertResult> result = EnhancedAlert.showTextInput( // Changed
                "Create " + currentNodeType.toString(),
                "Enter a unique label for the new " + currentNodeType.toString() + ":",
                nodeLabel // Pass default text to showTextInput
        );

        if (result.isPresent() && result.get().isOK()) {
          String newName = result.get().getTextInput();
          if (newName == null || newName.trim().isEmpty()) {
            EnhancedAlert.showError( // Changed
                    "Invalid Input",
                    "You must provide a label for the new " + currentNodeType.toString() + "."
            );
            continue;
          }

          nodeLabel = newName.trim();
          boolean exists = isLabelUnique(null, nodeLabel);
          if (exists) {
            unique = true;
          } else {
            EnhancedAlert.showError( // Changed
                    "Duplicate Label",
                    "The label '" + nodeLabel + "' already exists. Please choose a different one."
            );
          }
        } else {
          // User cancelled (ESC or Cancel button)
          System.out.println("Node creation cancelled by user.");
          return; // Exit the method
        }
      }

      // If we exit the while loop, unique is true and nodeLabel is valid
      this.addNodeToGraph(createNode(nodeLabel, currentNodeType.toString(), point));
    }
  }

  @Override
  protected void onVertexRightClickAction(SmartGraphVertex<Node> vertex) {
    super.onVertexRightClickAction(vertex);

    Vertex<Node> element = vertex.getUnderlyingVertex();

    if (element != null) {
      // Create context menu
      ContextMenu contextMenu = new ContextMenu();

      // Create custom MenuItem with TextField for label editing
      CustomMenuItem editLabelItem = new CustomMenuItem();
      VBox editBox = new VBox(10);
      TextField labelField = new TextField(element.element().getName());
      labelField.setPrefWidth(200); // Preferred width to fit the context menu

      // Handle Enter key and focus lost to apply changes
      Runnable applyLabelChange = () -> {
        String newLabel = labelField.getText().trim();
        if (!setNodeLabel(element, newLabel)) {
          // If the label is not unique or empty, show an error message
          EnhancedAlert.showError("Invalid Label", "The label must be unique and cannot be empty."); // Changed
        }
        contextMenu.hide();
      };

      labelField.setOnAction(_ -> applyLabelChange.run());

      editBox.getChildren().add(labelField);
      editLabelItem.setContent(editBox);
      editLabelItem.setHideOnClick(false); // Keep menu open while editing

      MenuItem deleteItem = new MenuItem("Delete");
      deleteItem.setStyle("-fx-text-fill: #f38ba8; -fx-font-weight: bold;");
      deleteItem.setOnAction(_ -> {
        removeNodeFromGraph(element);
      });

      String currentType = element.element().getShapeType();
      // Add user/admin toggle for transitions only
      MenuItem userTypeItem = null;
      MenuItem startPlaceItem = null;
      MenuItem endPlaceItem = null;
      MenuItem normalPlaceItem = null;
      if (currentType.equals("Transition")) {
        TRANSITION_TYPE current = ((Transition) element.element()).getType();
        userTypeItem = new MenuItem(
                (current.equals(TRANSITION_TYPE.ADMIN)) ? "Change to User"
                        : "Change to Admin"); // Default, since not implemented yet
        userTypeItem.setOnAction(_ -> {
          TRANSITION_TYPE newType = (current.equals(TRANSITION_TYPE.ADMIN)) ? TRANSITION_TYPE.USER
                  : TRANSITION_TYPE.ADMIN;
          setTransitionType(newType, element);
        });
        IconUtils.setIcon(userTypeItem, !current.equals(TRANSITION_TYPE.ADMIN) ? "AdminMode.png" : "UserMode.png", 20);
      } else {
        normalPlaceItem = new MenuItem("Set as Normal Place");
        IconUtils.setIcon(normalPlaceItem, "Place.png", 20);
        normalPlaceItem.setOnAction(_ -> {
          setPlaceType(PLACE_TYPE.NORMAL, element);
          if (petriNetBuilder.getStartNode() != null) {
            if (petriNetBuilder.getStartNode().getName().equals(element.element().getName())) {
              petriNetBuilder.setStartNode(null);
            }
          }
          if (petriNetBuilder.getFinishNode() != null) {
            if (petriNetBuilder.getFinishNode().getName().equals(element.element().getName())) {
              petriNetBuilder.setFinishNode(null);
            }
          }
        });

        startPlaceItem = new MenuItem("Set as Start Place");
        IconUtils.setIcon(startPlaceItem, "StartPlace.png", 20);
        startPlaceItem.setOnAction(_ -> {
          if (petriNetBuilder.getStartNode() != null) {
            Vertex<Node> node = getVertexByName(petriNetBuilder.getStartNode().getName());
            setPlaceType(PLACE_TYPE.NORMAL, node);
          }

          setPlaceType(PLACE_TYPE.START, element);
          if (petriNetBuilder.getFinishNode() != null) {
            if (petriNetBuilder.getFinishNode().getName().equals(element.element().getName())) {
              petriNetBuilder.setFinishNode(null);
            }
          }
          petriNetBuilder.setStartNode(element.element().getName());
        });

        endPlaceItem = new MenuItem("Set as Finish Place");
        IconUtils.setIcon(endPlaceItem, "EndPlace.png", 20);
        endPlaceItem.setOnAction(_ -> {
          if (petriNetBuilder.getFinishNode() != null) {
            Vertex<Node> node = getVertexByName(petriNetBuilder.getFinishNode().getName());
            setPlaceType(PLACE_TYPE.NORMAL, node);
          }
          if (petriNetBuilder.getStartNode() != null) {
            if (petriNetBuilder.getStartNode().getName().equals(element.element().getName())) {
              petriNetBuilder.setStartNode(null);
            }
          }
          setPlaceType(PLACE_TYPE.END, element);
          petriNetBuilder.setFinishNode(element.element().getName());
        });
      }

      MenuItem infoItem = new MenuItem("Show Info");
      infoItem.setOnAction(_ -> {
        EnhancedAlert.showInformation("Node Information", // Changed
                "Name: " + element.element().getName() +
                        "\nType: " + element.element().getShapeType() +
                        "\nVertex ID: " + element.toString());
      });

      if (userTypeItem != null) {
        contextMenu.getItems().addAll(editLabelItem,
                userTypeItem, new SeparatorMenuItem(), infoItem, deleteItem);
      } else {
        contextMenu.getItems().addAll(editLabelItem, normalPlaceItem, startPlaceItem, endPlaceItem,
                new SeparatorMenuItem(), infoItem, deleteItem);
      }

      // Show context menu at cursor position
      showContextMenuOnGraph(contextMenu, element);
      // Focus the text field after showing menu
      labelField.requestFocus();
      labelField.selectAll();
    }
  }

  @Override
  protected void onVertexSingleClickAction(SmartGraphVertex<Node> vertex) {
    super.onVertexSingleClickAction(vertex);
    if (currentMode.equals(MODE.CONNECT)) {
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
          boolean edgeExists = doesEdgeExist(firstSelectedVertex, element);

          if (!edgeExists) {
            String edgeLabel = "edge_" + System.currentTimeMillis(); // Unique edge label
            // only connection with place -> transition or transition -> place are allowed
            if (!areCompatible(firstSelectedVertex.element(), element.element())) {
              EnhancedAlert.showError("Connection Error", // Changed
                      "Cannot connect " + firstSelectedVertex.element().getName() +
                              " to " + element.element().getName() +
                              ". Only Place to Transition or Transition to Place connections are allowed.");
              firstSelectedVertex = null; // Reset selection
              return;
            }
            addArcToGraph(firstSelectedVertex.element().getName(), element.element().getName(), edgeLabel);
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
      Vertex<Node> element = vertex.getUnderlyingVertex();

      if (element != null) {
        removeNodeFromGraph(element);
      }
    } else {
      // In CREATE mode, just show vertex info
      System.out.println("Click: " + vertex.getStylableLabel());
      System.out.println("------");
    }
  }

  @Override
  protected void removeNodeFromGraph(Vertex<Node> node) {
    super.removeNodeFromGraph(node);
    petriNetBuilder.removeNode(node.element().getName());
  }

  @Override
  protected void onEdgeSingleClickAction(SmartGraphEdge<String, Node> edge) {
    super.onEdgeSingleClickAction(edge);
    if (currentMode.equals(MODE.DELETION)) {
      var element = edge.getUnderlyingEdge();
      if (element != null) {
        removeEdgeFromGraph(element);
      }
    } else {
      System.out.println("Selected edge: " + edge);
    }
  }

  @Override
  protected void removeEdgeFromGraph(Edge<String, Node> edge) {
    super.removeEdgeFromGraph(edge);
    petriNetBuilder.removeArc(edge.vertices()[0].element().getName(),
            edge.vertices()[1].element().getName());
  }

  @Override
  protected void addArcToGraph(String from, String to, String label) {
    super.addArcToGraph(from, to, label);
    petriNetBuilder.addArc(from, to);
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

  private void prepareNodes() {
    getGraphVertices().forEach(vtx -> {
      Point2D pos = getVertexPosition(vtx);
      int x = (int) pos.getX();
      int y = (int) pos.getY();

      petriNetBuilder.setNodePosition(vtx.element().getName(), x, y);
    });
  }

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

  private boolean areCompatible(Node node1, Node node2) {
    if (node1 instanceof Place && node2 instanceof Transition) {
      return true; // Place can connect to Transition
    } else
      return node1 instanceof Transition && node2 instanceof Place; // Transition can connect to Place
    // Other combinations are not allowed
  }

  // Removed the old PopupMenu methods as they are no longer needed
  // EnhancedAlert handles all the dialog functionality now
}