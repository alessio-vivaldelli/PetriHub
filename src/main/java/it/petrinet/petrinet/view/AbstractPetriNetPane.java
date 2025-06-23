package it.petrinet.petrinet.view;

import java.util.Collection;

import com.brunomnsilva.smartgraph.containers.ContentZoomScrollPane;
import com.brunomnsilva.smartgraph.graph.DigraphEdgeList;
import com.brunomnsilva.smartgraph.graph.Edge;
import com.brunomnsilva.smartgraph.graph.Graph;
import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphEdge;
import com.brunomnsilva.smartgraph.graphview.SmartGraphPanel;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import com.brunomnsilva.smartgraph.graphview.SmartPlacementStrategy;
import com.brunomnsilva.smartgraph.graphview.SmartRandomPlacementStrategy;

import atlantafx.base.theme.PrimerLight;
import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PLACE_TYPE;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.geometry.Point2D;
import javafx.scene.control.ContextMenu;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

/**
 * An abstract base class for displaying Petri Nets.
 * <p>
 * This class encapsulates the common components and logic for rendering a
 * graph,
 * including the display panel, the graph data model, and zoom/pan controls.
 * It uses the "Template Method" pattern to define the structure of user
 * interactions,
 * allowing subclasses to provide specific implementations for events like
 * clicks
 * on nodes, edges, or the canvas.
 * </p>
 */
public abstract class AbstractPetriNetPane extends Pane {

  private static final String USER_TRANSITION_STYLE = "userTransition";
  private static final String ADMIN_TRANSITION_STYLE = "adminTransition";
  private static final String START_VERTEX_STYLE = "startVertex";
  private static final String END_VERTEX_STYLE = "endVertex";
  private static final String NORMAL_VERTEX_STYLE = "vertex";

  /** The JavaFX component responsible for rendering the graph. */
  private SmartGraphPanel<Node, String> graphView;
  /** The underlying data model of the graph, containing vertices and edges. */
  private DigraphEdgeList<Node, String> g;

  /**
   * A wrapper panel for {@link #graphView} that provides zoom and pan
   * functionality.
   */
  private ContentZoomScrollPane contentZoomScrollPane;

  /** A flag to enable or disable user interactions with the graph. */
  private boolean isInteractionEnabled = true;

  /**
   * Default constructor.
   */
  public AbstractPetriNetPane() {
    setupGraph();
    setupInteraction();
    this.setPrefSize(1920, 1080);
  }

  /**
   * Initializes the main graph components.
   * Creates the graph model, the view panel, and adds them to the scene.
   */
  private final void setupGraph() {
    g = new DigraphEdgeList<>();
    SmartPlacementStrategy initialPlacement = new SmartRandomPlacementStrategy();
    graphView = new SmartGraphPanel<>(g, initialPlacement);
    contentZoomScrollPane = new ContentZoomScrollPane(graphView);

    graphView.setPrefHeight(1080);

    VBox vBox = new VBox();
    vBox.getChildren().add(contentZoomScrollPane);
    vBox.prefWidthProperty().bind(this.widthProperty());
    vBox.prefHeightProperty().bind(this.heightProperty());
    this.getChildren().add(vBox);
  }

  /**
   * Binds user interaction events from the {@link #graphView} to the
   * corresponding
   * "on...Click" hook methods in this class.
   */
  private final void setupInteraction() {
    graphView.setCanvasSingleClickAction(e -> onCanvasSingleClickAction(e));
    graphView.setEdgeSingleClickAction(this::onEdgeSingleClickAction);
    graphView.setVertexSingleClickAction(this::onVertexSingleClickAction);
    graphView.setVertexRightClickAction(this::onVertexRightClickAction);
  }

  private final void removeInteraction() {
    graphView.setCanvasSingleClickAction(null);
    graphView.setEdgeSingleClickAction(null);
    graphView.setVertexSingleClickAction(null);
    graphView.setVertexRightClickAction(null);
  }

  /**
   * Enables user interactions with the graph.
   */
  protected final void enableInteraction() {
    isInteractionEnabled = true;
    setupInteraction();
  }

  /**
   * Disables user interactions with the graph.
   */
  protected final void disableInteraction() {
    isInteractionEnabled = false;
    removeInteraction();
  }

  /**
   * Returns the current state of user interactions.
   *
   * @return {@code true} if interactions are enabled, {@code false} otherwise.
   */
  public boolean getIsInteractionEnabled() {
    return isInteractionEnabled;
  }

  /**
   * Hook method called on a vertex right-click. Subclasses should override this
   * to implement specific logic (e.g., show a context menu).
   *
   * @param vertex The clicked vertex.
   */
  protected void onVertexRightClickAction(SmartGraphVertex<Node> vertex) {
    if (!isInteractionEnabled)
      return;
  }

  /**
   * Hook method called on a single vertex click. Subclasses should override this
   * to implement specific logic (e.g., select a node).
   *
   * @param vertex The clicked vertex.
   */
  protected void onVertexSingleClickAction(SmartGraphVertex<Node> vertex) {
    if (!isInteractionEnabled)
      return;
  }

  /**
   * Hook method called on a single edge click. Subclasses should override this
   * to implement specific logic (e.g., delete an edge).
   *
   * @param edge The clicked edge.
   */
  protected void onEdgeSingleClickAction(SmartGraphEdge<String, Node> edge) {
    if (!isInteractionEnabled)
      return;
  }

  /**
   * Hook method called on a single canvas click. Subclasses should override this
   * to implement specific logic (e.g., create a new node).
   *
   * @param point The clicked point coordinates on the canvas.
   */
  protected void onCanvasSingleClickAction(Point2D point) {
    if (!isInteractionEnabled)
      return;
  }

  /**
   * Hook method called after the graph visualization components are fully
   * initialized.
   * Subclasses can override this to perform actions once the graph is ready.
   */
  protected void onGraphInitialized() {
    // Default implementation is empty.
  }

  /**
   * Performs the final initialization of the graph view component.
   * This includes setting layout properties and the initial zoom level.
   */
  private final void initializeGraphComponent() {
    graphView.init();
    graphView.setAutomaticLayout(true);
    graphView.setAutomaticLayout(false);
    graphView.setPrefHeight(1080);

    graphView.update();

    contentZoomScrollPane.zoomIn();
  }

  /**
   * Adds a new node to the graph model and updates the view.
   *
   * @param element The node element to add.
   * @return The created {@link Vertex} in the graph.
   */
  protected Vertex<Node> addNodeToGraph(Node element) {
    if (element == null) {
      return null;
    }
    Vertex<Node> newVertex = g.insertVertex(element);
    graphView.updateAndWait();

    Vertex<Node> v = graphView.getModel().vertices().stream()
        .filter(vtx -> vtx == newVertex).findFirst().orElse(null);

    if (v == null)
      return null;
    if (element instanceof Transition t) {
      graphView.getStylableVertex(v)
          .setStyleClass((t.getType().equals(TRANSITION_TYPE.USER)) ? USER_TRANSITION_STYLE : ADMIN_TRANSITION_STYLE);
    } else if (element instanceof Place p) {
      if (p.isEndPlace() || p.isStartPlace()) {
        graphView.getStylableVertex(v).setStyleClass(p.isStartPlace() ? START_VERTEX_STYLE : END_VERTEX_STYLE);
      }
    }
    graphView.setVertexPosition(v, element.getPosition().getX(), element.getPosition().getY());

    graphView.updateAndWait();
    graphView.update();

    return newVertex;
  }

  /**
   * Creates a directed edge (arc) between two vertices.
   *
   * @param from  The source vertex.
   * @param to    The destination vertex.
   * @param label The label for the new arc.
   */
  protected void addArcToGraph(Vertex<Node> from, Vertex<Node> to, String label) {
    g.insertEdge(from, to, label);
    graphView.update();
  }

  /**
   * Creates a directed edge (arc) between two vertices.
   *
   * @param from  The source vertex.
   * @param to    The destination vertex.
   * @param label The label for the new arc.
   */
  protected void addArcToGraph(Node from, Node to, String label) {
    g.insertEdge(from, to, label);
    graphView.update();
  }

  /**
   * Creates a directed edge (arc) between two vertices.
   *
   * @param from  The source vertex.
   * @param to    The destination vertex.
   * @param label The label for the new arc.
   */
  protected void addArcToGraph(String from, String to, String label) {
    this.addArcToGraph(getVertexByName(from), getVertexByName(to), label);
  }

  /**
   * Removes a node and its incident edges from the graph.
   *
   * @param node The vertex to remove.
   */
  protected void removeNodeFromGraph(Vertex<Node> node) {
    g.removeVertex(node);
    graphView.update();
  }

  /**
   * Removes an edge from the graph.
   *
   * @param edge The edge to remove.
   */
  protected void removeEdgeFromGraph(Edge<String, Node> edge) {
    g.removeEdge(edge);
    graphView.update();
  }

  protected boolean setNodeLabel(Vertex<Node> node, String newLabel) {
    if (!newLabel.isEmpty() && !newLabel.equals(node.element().getName()) && isLabelUnique(node, newLabel)) {
      node.element().setName(newLabel);
      graphView.update();
      return true;
    }
    return false;

  }

  protected void setTransitionType(TRANSITION_TYPE type, Vertex<Node> transition) {
    if (transition.element() instanceof Transition t) {
      t.setType(type);
      if (type.equals(TRANSITION_TYPE.USER)) {
        graphView.getStylableVertex(transition.element()).setStyleClass(USER_TRANSITION_STYLE);

      } else {
        graphView.getStylableVertex(transition.element()).setStyleClass(ADMIN_TRANSITION_STYLE);

      }
    }
    graphView.update();

  }

  protected void setPlaceType(PLACE_TYPE type, Vertex<Node> place) {
    if (place.element() instanceof Place p) {
      p.setType(type);
      if (type.equals(PLACE_TYPE.START)) {

        graphView.getStylableVertex(place.element()).setStyleClass(START_VERTEX_STYLE);
      } else if (type.equals(PLACE_TYPE.END)) {

        graphView.getStylableVertex(place.element()).setStyleClass(END_VERTEX_STYLE);
      } else {

        graphView.getStylableVertex(place.element()).setStyleClass(NORMAL_VERTEX_STYLE);
      }
    }
    graphView.update();
  }

  protected final Vertex<Node> getVertexByName(String name) {
    return getGraphVertices().stream()
        .filter(
            vtx -> vtx.element().getName().equals(name))
        .findFirst().orElse(null);
  }

  protected final void showContextMenuOnGraph(ContextMenu contextMenu, Vertex<Node> element) {
    contextMenu.show(graphView, graphView.getScene().getWindow().getX() + graphView.getVertexPositionX(element),
        graphView.getScene().getWindow().getY() + graphView.getVertexPositionY(element));

  }

  // Checks if the given label is unique among all elements in the graph
  protected boolean isLabelUnique(Vertex<Node> node, String label) {
    return getGraphVertices().stream()
        .noneMatch(e -> e.element().getName().equals(label) && e != node);
  }

  protected final boolean doesEdgeExist(Vertex<Node> from, Vertex<Node> to) {
    return getGraphEdges().stream()
        .anyMatch(edge -> (g.opposite(from, edge) == to) ||
            (g.opposite(to, edge) == from));

  }

  /**
   * Zooms in the canvas view.
   */
  public void zoomInAction() {
    if (contentZoomScrollPane == null)
      return;
    // System.out.println("Zoom in, current scale: " +
    // contentZoomScrollPane.scaleFactorProperty().get());
    contentZoomScrollPane.zoomIn();
  }

  protected final Collection<Vertex<Node>> getGraphVertices() {
    return graphView.getModel().vertices();
  }

  protected final Point2D getVertexPosition(Vertex<Node> node) {
    return new Point2D(graphView.getVertexPositionX(node), graphView.getVertexPositionY(node));
  }

  protected final Collection<Edge<String, Node>> getGraphEdges() {
    return graphView.getModel().edges();
  }

  /**
   * Zooms out the canvas view.
   */
  public void zoomOutAction() {
    if (contentZoomScrollPane == null)
      return;
    // System.out.println("Zoom out, current scale: " +
    // contentZoomScrollPane.scaleFactorProperty().get());
    contentZoomScrollPane.zoomOut();
  }

  /**
   * Gets the current zoom level of the view.
   *
   * @return The current zoom level.
   */
  public double getZoomLevel() {
    if (contentZoomScrollPane == null)
      return 1.0;
    return contentZoomScrollPane.scaleFactorProperty().get();
  }

  /**
   * This is the main entry point to make the component ready for use.
   * This method must be called after the scene is fully laid out
   */
  public final void init() {

    // Use a short delay to ensure the scene is properly laid out before
    // initializing the graph view
    PauseTransition delay = new PauseTransition(Duration.millis(200));
    delay.setOnFinished(_ -> {
      initializeGraphComponent();
      onGraphInitialized(); // Call the hook after initialization is complete
    });
    delay.play();
  }
}
