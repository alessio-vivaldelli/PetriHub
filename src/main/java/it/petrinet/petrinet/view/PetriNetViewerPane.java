package it.petrinet.petrinet.view;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import com.brunomnsilva.smartgraph.graphview.SmartGraphVertex;

import it.petrinet.exceptions.InputTypeException;
import it.petrinet.model.Computation;
import it.petrinet.model.ComputationStep;
import it.petrinet.model.database.ComputationStepDAO;
import it.petrinet.model.database.ComputationsDAO;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.Node;
import it.petrinet.petrinet.model.PLACE_TYPE;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.TRANSITION_TYPE;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.persistance.pnml.PNMLParser;
import it.petrinet.view.ViewNavigator;

public class PetriNetViewerPane extends AbstractPetriNetPane {

  private Computation computation;
  private PetriNetModel petriNetModel;
  private String petriName;
  private final String petriNetPNML;
  private Map<String, Integer> marking;
  private boolean testMode = true;

  private Consumer<ComputationStep> onTransitionFired = null;
  private Consumer<String> onNewFirableTransitionAviable = null;
  private Consumer<String> onPetriNetFinished = null;

  public PetriNetViewerPane(String petriNetPNML, Computation computation) {
    super();
    this.petriNetPNML = petriNetPNML;

    if (!(new File(petriNetPNML)).exists()) {
      throw new IllegalArgumentException("File does not exist: " + petriNetPNML);
    }

    if (computation == null) {
      disableInteraction();
    }
    setComputation(computation);
  }

  public PetriNetViewerPane(String petriNetPNML) {
    this(petriNetPNML, null);
  }

  public void setComputation(Computation computation) {
    this.computation = computation;
    if (computation == null) {
      disableInteraction();
    } else {
      enableInteraction();
    }
  }

  @Override
  protected void onGraphInitialized() {
    super.onGraphInitialized();
    marking = computation.getSteps().getLast().getMarkingState();

    PNMLParser parser = new PNMLParser();
    try {
      this.petriNetModel = parser.parse(petriNetPNML);
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.petriName = petriNetModel.getName();

    petriNetModel.getNodes().forEach(node -> {
      if (node instanceof Place p) {
        if (marking.containsKey(p.getName())) {
          p.setPlaceTokens(marking.get(p.getName()));
        } else {
          p.setPlaceTokens(0);
        }
        addNodeToGraph(p);
      } else if (node instanceof Transition t) {
        addNodeToGraph(t);
      }
    });

    petriNetModel.getConnections().forEach((from, toNodes) -> {
      toNodes.forEach(to -> {
        addArcToGraph(from.getName(), to.getName());
      });
    });

    computeFirableTransitions();
  }

  @Override
  protected void onVertexSingleClickAction(SmartGraphVertex<Node> vertex) {
    super.onVertexSingleClickAction(vertex);

    if (vertex.getUnderlyingVertex().element() instanceof Transition t) {
      if (t.getIsReadyToFire()) {
        incidentEdges(vertex.getUnderlyingVertex()).stream()
            .map(edge -> (edge.vertices()[0].equals(vertex.getUnderlyingVertex()) ? edge.vertices()[1]
                : edge.vertices()[0]))
            .forEach(node -> {
              if (node.element() instanceof Place p) {
                p.setPlaceTokens(0);
                marking.put(p.getName(), 0);
              } else {
                System.out.println("Strange error....");
              }
            });
        outboundEdges(vertex.getUnderlyingVertex()).stream()
            .map(edge -> (edge.vertices()[0].equals(vertex.getUnderlyingVertex()) ? edge.vertices()[1]
                : edge.vertices()[0]))
            .forEach(node -> {
              if (node.element() instanceof Place p) {
                if (p.getType().equals(PLACE_TYPE.END)) {
                  handleFinishPlaceReached();
                }
                p.incrementPlaceTokens();
                marking.put(p.getName(), p.getPlaceTokens());
              } else {
                System.out.println("Strange error....");
              }
            });
        updateGraph();
        t.setIsFirable(false);
        setNodeStyle(vertex.getUnderlyingVertex(),
            (t.getType().equals(TRANSITION_TYPE.ADMIN)) ? ADMIN_TRANSITION_STYLE : USER_TRANSITION_STYLE);
        computeFirableTransitions();

        if (!testMode) {
          ComputationStep nStep = null;
          try {
            nStep = new ComputationStep(ComputationsDAO.getIdByComputation(computation),
                computation.getNetId(), vertex.getUnderlyingVertex().element().getName(), marking,
                System.currentTimeMillis() / 1000);
            computation.getSteps().add(nStep);
          } catch (InputTypeException e) {
            e.printStackTrace();
          }
          if (onTransitionFired != null) {
            onTransitionFired.accept(nStep);
          }
        }
      }
    }
  }

  private void handleFinishPlaceReached() {
    // TODO: implement logic to handle finish
    if (onPetriNetFinished != null) {
      onPetriNetFinished.accept("arg0");
    }
    System.out.println("petri net finished....");
  }

  private void computeFirableTransitions() {
    if (marking == null) {
      return;
    }

    getGraphVertices().stream().forEach(vertex -> {
      if (vertex.element() instanceof Transition t) {
        if (incidentEdges(vertex).stream()
            .map(edge -> (edge.vertices()[0].equals(vertex) ? edge.vertices()[1] : edge.vertices()[0]))
            .allMatch(node -> ((Place) node.element()).getPlaceTokens() > 0)) {
          System.out.println("TRANSITION " + t + " is FIRABLE");
          removeNodeStyle(vertex, USER_TRANSITION_STYLE);
          removeNodeStyle(vertex, ADMIN_TRANSITION_STYLE);
          addNodeStyle(vertex, FIRABLE_TRANSITION_STYLE);
          if (testMode) {
            t.setIsFirable(true);
          } else {
            if (ViewNavigator.getAuthenticatedUser().getUsername().equals(computation.getCreatorId())
                && t.getType().equals(TRANSITION_TYPE.ADMIN)) {
              t.setIsFirable(true);
            }
            if (ViewNavigator.getAuthenticatedUser().getUsername().equals(computation.getUserId())
                && t.getType().equals(TRANSITION_TYPE.USER)) {
              t.setIsFirable(true);
            }
          }
        }
      }
    });
  }

}
