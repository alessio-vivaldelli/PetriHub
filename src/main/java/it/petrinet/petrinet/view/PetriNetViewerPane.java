package it.petrinet.petrinet.view;

import com.brunomnsilva.smartgraph.graph.Vertex;
import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;
import it.petrinet.model.Computation;
import it.petrinet.petrinet.model.*;
import it.petrinet.petrinet.persistance.pnml.PNMLParser;
import it.petrinet.view.ViewNavigator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A custom exception for errors during the view initialization.
 */
class PetriNetViewException extends RuntimeException {
  public PetriNetViewException(String message, Throwable cause) {
    super(message, cause);
  }
}

public class PetriNetViewerPane extends AbstractPetriNetPane {

  /**
   * Represents an operation that accepts three input arguments and returns no
   * result.
   * 
   * @param <T> the type of the first argument
   * @param <U> the type of the second argument
   * @param <V> the type of the third argument
   */
  @FunctionalInterface
  public interface TriConsumer<T, U, V> {
    void accept(T t, U u, V v);
  }

  private final String petriNetPNML;
  private Computation computation;
  private PetriNetModel petriNetModel;
  private boolean testMode = false; // Kept for authorization logic

  // Callbacks
  private TriConsumer<String, Map<String, Integer>, List<Transition>> onTransitionFired;
  private Runnable onPetriNetFinished;

  public PetriNetViewerPane(String petriNetPNML, Computation computation) {
    super();
    if (!(new File(petriNetPNML)).exists()) {
      throw new IllegalArgumentException("File does not exist: " + petriNetPNML);
    }
    this.petriNetPNML = petriNetPNML;
    this.computation = computation;
    this.enableInteraction(computation != null);
  }

  public PetriNetViewerPane(String petriNetPNML) {
    this(petriNetPNML, null);
  }

  /**
   * This method must be call after the graph is initialized
   *
   * @param computation
   */
  public List<Transition> setComputation(Computation computation) {

    this.computation = computation;
    this.enableInteraction(computation != null);

    Map<String, Integer> initialMarking = (computation != null && !computation.getSteps().isEmpty())
        ? computation.getSteps().getLast().getMarkingState()
        : Map.of();

    petriNetModel.getNodes().forEach(node -> {
      if (node instanceof Place p) {
        p.setPlaceTokens(initialMarking.getOrDefault(p.getName(), 0));
      }
    });
    updateGraph();

    return computeAndApplyFirableTransitions();
  }

  /**
   * Call this method to update tokens status. For example it can ben called after
   * first subscription to the net to refresh "last computation step" in the
   * computation
   * 
   */
  public void updateComputation() {
    Map<String, Integer> initialMarking = (computation != null && !computation.getSteps().isEmpty())
        ? computation.getSteps().getLast().getMarkingState()
        : Map.of();

    petriNetModel.getNodes().forEach(node -> {
      if (node instanceof Place p) {
        p.setPlaceTokens(initialMarking.getOrDefault(p.getName(), 0));
      }
    });

  }

  @Override
  protected void onGraphInitialized() {
    super.onGraphInitialized();
    try {
      loadModelAndBuildGraph();
      computeAndApplyFirableTransitions();
    } catch (IOException e) {
      this.enableInteraction(false);
      throw new PetriNetViewException("Failed to initialize Petri Net view", e);
    }
  }

  private void enableInteraction(boolean newValue) {
    if (newValue) {
      enableInteraction();
      return;
    }
    disableInteraction();
  }

  public String getStartPlaceName() {
    return this.petriNetModel.getStartNode().getName();
  }

  public String getFinishPlaceName() {
    return this.petriNetModel.getFinishNode().getName();
  }

  private void loadModelAndBuildGraph() throws IOException {
    this.petriNetModel = new PNMLParser().parse(petriNetPNML);

    Map<String, Integer> initialMarking = (computation != null && !computation.getSteps().isEmpty())
        ? computation.getSteps().getLast().getMarkingState()
        : Map.of();

    petriNetModel.getNodes().forEach(node -> {
      if (node instanceof Place p) {
        p.setPlaceTokens(initialMarking.getOrDefault(p.getName(), 0));
      }
      addNodeToGraph(node);
    });

    petriNetModel.getConnections()
        .forEach((from, toNodes) -> toNodes.forEach(to -> addArcToGraph(from.getName(), to.getName())));
  }

  @Override
  protected void onVertexSingleClickAction(SmartGraphVertex<Node> vertex) {
    super.onVertexSingleClickAction(vertex);
    Node element = vertex.getUnderlyingVertex().element();

    if (element instanceof Transition t && t.getIsReadyToFire()) {
      fireTransition(t, vertex.getUnderlyingVertex());
    }
  }

  private void fireTransition(Transition t, Vertex<Node> vertex) {
    // 1. Update model state (consume and produce tokens)
    Map<String, Integer> newMarking = consumeTokensAndGetCurrentMarking(vertex);
    produceTokens(vertex, newMarking);

    // 2. Update view
    updateGraph();
    t.setIsFirable(false);
    String style = (t.getType() == TRANSITION_TYPE.ADMIN) ? ADMIN_TRANSITION_STYLE : USER_TRANSITION_STYLE;
    setNodeStyle(vertex, style);

    // 3. Re-calculate firable transitions and notify listeners
    List<Transition> firableTransitions = computeAndApplyFirableTransitions();
    if (onTransitionFired != null) {
      onTransitionFired.accept(t.getName(), newMarking, firableTransitions);
    }
  }

  private Map<String, Integer> consumeTokensAndGetCurrentMarking(Vertex<Node> transitionVertex) {
    Map<String, Integer> currentMarking = getGraphVertices().stream()
        .filter(v -> v.element() instanceof Place)
        .map(v -> (Place) v.element())
        .filter(p -> p.getPlaceTokens() > 0)
        .collect(Collectors.toMap(Place::getName, Place::getPlaceTokens));

    incidentEdges(transitionVertex).stream()
        .map(edge -> getOppositeVertex(edge, transitionVertex))
        .forEach(placeVertex -> {
          if (placeVertex.element() instanceof Place p) {
            p.setPlaceTokens(0); // Assumes weight of 1 and consuming all tokens
            currentMarking.remove(p.getName());
          } else {
            throw new IllegalStateException("Transition input must be a Place.");
          }
        });
    return currentMarking;
  }

  private void produceTokens(Vertex<Node> transitionVertex, Map<String, Integer> marking) {
    outboundEdges(transitionVertex).stream()
        .map(edge -> getOppositeVertex(edge, transitionVertex))
        .forEach(placeVertex -> {
          if (placeVertex.element() instanceof Place p) {
            p.incrementPlaceTokens();
            marking.put(p.getName(), p.getPlaceTokens());
            if (p.getType() == PLACE_TYPE.END && onPetriNetFinished != null) {
              onPetriNetFinished.run();
              disableInteraction();
            }
          } else {
            throw new IllegalStateException("Transition output must be a Place.");
          }
        });
  }

  public TRANSITION_TYPE getTypeByTransitionName(String transition) {
    if(transition.isEmpty() || transition.isBlank()){return TRANSITION_TYPE.USER;}
    if(petriNetModel.getNodeByName(transition) instanceof Transition t ){
      return t.getType();
    }
    return null;
  }

  private List<Transition> computeAndApplyFirableTransitions() {
    if (computation != null) {
      if(computation.isFinished()){
        disableInteraction();
        return new ArrayList<>();
      }
    }
    List<Transition> firableTransitions = new ArrayList<>();
    getGraphVertices().forEach(vertex -> {
      if (vertex.element() instanceof Transition t) {
        setNodeStyle(vertex, (t.getType().equals(TRANSITION_TYPE.USER) ? USER_TRANSITION_STYLE : ADMIN_TRANSITION_STYLE));
        boolean isEnabled = incidentEdges(vertex).stream()
            .map(edge -> getOppositeVertex(edge, vertex))
            .allMatch(node -> ((Place) node.element()).getPlaceTokens() > 0);

        if (isEnabled){
          firableTransitions.add(t);
        }

        if (isEnabled && isUserAllowedToFire(t)) {
          t.setIsFirable(true);
          removeNodeStyle(vertex, ADMIN_TRANSITION_STYLE);
          removeNodeStyle(vertex, USER_TRANSITION_STYLE);

          addNodeStyle(vertex, FIRABLE_TRANSITION_STYLE);
        }
        else {
          t.setIsFirable(false);
          removeNodeStyle(vertex, FIRABLE_TRANSITION_STYLE);
        }
      }
    });
    return firableTransitions;
  }

  private boolean isUserAllowedToFire(Transition t) {
    if (testMode)
      return true;

    String username = ViewNavigator.getAuthenticatedUser().getUsername();
    if (t.getType() == TRANSITION_TYPE.ADMIN) {
      return username.equals(computation.getCreatorId());
    }
    if (t.getType() == TRANSITION_TYPE.USER) {
      return username.equals(computation.getUserId());
    }
    return false;
  }

  /**
   * Sets a callback for when a transition is fired.
   * 
   * @param onTransitionFired A TriConsumer that accepts the fired transition's
   *                          name,
   *                          the new marking state, and a list of newly firable
   *                          transitions.
   */
  public void setOnTransitionFired(TriConsumer<String, Map<String, Integer>, List<Transition>> onTransitionFired) {
    this.onTransitionFired = onTransitionFired;
  }

  /**
   * Sets a callback for when the Petri Net reaches a finish place.
   * 
   * @param onPetriNetFinished A Runnable to be executed.
   */
  public void setOnPetriNetFinished(Runnable onPetriNetFinished) {
    this.onPetriNetFinished = onPetriNetFinished;
  }

}
