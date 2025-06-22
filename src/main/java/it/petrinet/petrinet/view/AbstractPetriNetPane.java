package it.petrinet.petrinet.view;

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

import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import javafx.animation.PauseTransition;
import javafx.geometry.Point2D;
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

  /** The JavaFX component responsible for rendering the graph. */
  private SmartGraphPanel<Node, String> graphView;

  /**
   * A reference to the first vertex selected during a connection operation.
   * This is intended to be managed by subclasses that implement a connection
   * mode.
   */
  private Vertex<Node> firstSelectedVertex = null;

  /** The underlying data model of the graph, containing vertices and edges. */
  private Graph<Node, String> g;

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

    VBox vBox = new VBox(contentZoomScrollPane);
    vBox.prefWidthProperty().bind(this.widthProperty());
    vBox.prefHeightProperty().bind(this.heightProperty());

    this.getChildren().clear();
    this.getChildren().add(vBox);
  }

  /**
   * Binds user interaction events from the {@link #graphView} to the
   * corresponding
   * "on...Click" hook methods in this class.
   */
  private final void setupInteraction() {
    graphView.setCanvasSingleClickAction(this::onCanvasSingleClickAction);
    graphView.setEdgeSingleClickAction(this::onEdgeSingleClickAction);
    graphView.setVertexSingleClickAction(this::onVertexSingleClickAction);
    graphView.setVertexRightClickAction(this::onVertexRightClickAction);
  }

  /**
   * Enables user interactions with the graph.
   */
  protected final void enableInteraction() {
    isInteractionEnabled = true;
  }

  /**
   * Disables user interactions with the graph.
   */
  protected final void disableInteraction() {
    isInteractionEnabled = false;
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

    Vertex<Node> v = graphView.getModel().vertices().stream()
        .filter(vtx -> vtx == newVertex).findFirst().orElse(null);

    if (v == null)
      return null;
    if (element instanceof Transition t) {
      graphView.getStylableVertex(v)
          .setStyleClass((t.getType().equals(TRANSITION_TYPE.USER)) ? "userTransition" : "adminTransition");
    } else if (element instanceof Place p) {
      if (p.isEndPlace() || p.isStartPlace()) {
        graphView.getStylableVertex(v).setStyleClass(p.isStartPlace() ? "startVertex" : "endVertex");
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
  protected void createArc(Vertex<Node> from, Vertex<Node> to, String label) {
    g.insertEdge(from, to, label);
    graphView.update();
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
   * Initializes the entire pane, setting up the graph, interactions, and
   * components.
   * This is the main entry point to make the component ready for use.
   */
  public void init() {
    setupGraph();
    setupInteraction();

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
