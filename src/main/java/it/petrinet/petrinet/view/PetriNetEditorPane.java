package it.petrinet.petrinet.view;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Consumer;

import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphEdge;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;

import it.petrinet.Main;
import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PLACE_TYPE;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.persistance.pnml.PNMLSerializer;
import it.petrinet.utils.IconUtils;
import it.petrinet.view.components.PopupMenu;
import javafx.geometry.Point2D;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import javax.swing.*;

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
      showMessage(AlertType.ERROR, "Error", "Empty Name",
          "Please provide a valid name for the Petri Net.");
      return; // Exit if name or description is empty
    }

    petriNetBuilder.setPetriName(name);

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

  // ... (nel metodo onCanvasSingleClickAction)

  @Override
  protected void onCanvasSingleClickAction(Point2D point) {
    super.onCanvasSingleClickAction(point);

    if (currentMode.equals(MODE.CREATE)) {
      String baseLabel = !currentNodeType.equals(NODE_TYPE.TRANSITION) ? "New Place" : "New Transition";
      boolean unique = false;
      String nodeLabel = baseLabel;

      // Loop per garantire un label unico
      while (!unique) {
        PopupMenu dialog = new PopupMenu(Main.getPrimaryStage());
        Label prompt = new Label();
        prompt.setText("Enter a unique label for the new " +
                (currentNodeType.equals(NODE_TYPE.TRANSITION) ? "Transition" : "Place") + ":");
        prompt.setStyle("-fx-text-fill: white; -fx-font-size: 14px;");
        TextField labelTextField = new TextField();
        labelTextField.setPromptText("name");

        // Aggiungi un pulsante OK per confermare l'input
        ButtonType okButton = new ButtonType("OK", ButtonBar.ButtonData.OK_DONE);
        ButtonType cancelButton = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        VBox inputContent = new VBox(10);
        inputContent.getChildren().addAll(prompt, labelTextField);

        // Aggiungi i pulsanti in un HBox (o in un FlowPane per più flessibilità)
        javafx.scene.control.Button okBtn = new javafx.scene.control.Button("OK");
        okBtn.setStyle("-fx-background-color: #4CAF50; -fx-text-fill: white;");
        javafx.scene.control.Button cancelBtn = new javafx.scene.control.Button("Cancel");
        cancelBtn.setStyle("-fx-background-color: #f44336; -fx-text-fill: white;");

        inputContent.getChildren().addAll(okBtn, cancelBtn); // Aggiungi i pulsanti

        dialog.setContent(inputContent);

        // Gestione del click sui pulsanti
        okBtn.setOnAction(e -> {
          dialog.setResult(labelTextField.getText().trim()); // Imposta il risultato e chiudi
        });
        cancelBtn.setOnAction(e -> {
          dialog.setResult(null); // Imposta il risultato a null e chiudi
        });

        // Puoi anche gestire Enter per l'OK
        labelTextField.setOnAction(e -> {
          dialog.setResult(labelTextField.getText().trim());
        });

        // MOST IMPORTANT CHANGE: Usa showAndWait()
        Optional<String> resultOptional = dialog.showAndWait();

        System.out.println("User input received (via Optional): " + resultOptional);

        if (resultOptional.isPresent()) {
          String tmpNodeLabel = resultOptional.get();
          if (tmpNodeLabel.isEmpty()) {
            showMessage(AlertType.ERROR, "Input Error", "Empty Label", "Label cannot be empty. Please try again.");
            continue; // Continua il loop
          }
          nodeLabel = tmpNodeLabel;
          boolean exists = isLabelUnique(null, tmpNodeLabel);
          if (exists) {
            unique = true;
          } else {
            showMessage(AlertType.WARNING, "Duplicate Label", "Label Not Unique", "The label '" + tmpNodeLabel + "' already exists. Please choose a different one.");
            // Non impostare nodeLabel a "copy" qui, lascia all'utente di riscrivere
          }
        } else {
          // Utente ha annullato (ESC o click fuori o pulsante Cancel)
          System.out.println("Popup cancelled by user.");
          return; // Esce dalla funzione onCanvasSingleClickAction
        }
      }

      // Se si esce dal while, significa che unique è true e nodeLabel è valido
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
      labelField.setPrefWidth(200); // Larghezza preferita per adattarsi al menu contestuale
      // Handle Enter key and focus lost to apply changes

      Runnable applyLabelChange = () -> {
        String newLabel = labelField.getText().trim();
        if (!setNodeLabel(element, newLabel)) {
          // If the label is not unique or empty, show an error message
          showMessage(AlertType.ERROR, "Invalid Label", "Label Change Error",
              "The label must be unique and cannot be empty.");
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
        IconUtils.setIcon(userTypeItem, !current.equals(TRANSITION_TYPE.ADMIN) ? "AdminMode.png" : "UserMode.png" , 20);
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

        startPlaceItem = new MenuItem("Set as Start Palace");
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
        showMessage(AlertType.INFORMATION, "Node Information", "Node Details",
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
          boolean edgeExists = doesEdgeExist(firstSelectedVertex, element);

          if (!edgeExists) {
            String edgeLabel = "edge_" + System.currentTimeMillis(); // Unique edge label
            // only connection with place -> transition or transition -> place are allowed
            if (!areCompatible(firstSelectedVertex.element(), element.element())) {
              showMessage(AlertType.ERROR, "Connection Error",
                  "Incompatible Nodes",
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
      // Vertex<Node> element = graphView.getModel().vertices().stream()
      // .filter(vtx -> vtx == vertex.getUnderlyingVertex()).findFirst().orElse(null);
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

  private Optional<ButtonType> showMessage(AlertType type, String title, String headerText, String message) {
    Alert alert = new Alert(type);
    alert.setTitle("Error");
    alert.setHeaderText("An error occurred");
    alert.setContentText(message);
    return alert.showAndWait();
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
    } else return node1 instanceof Transition && node2 instanceof Place; // Transition can connect to Place
// Other combinations are not allowed
  }

}
