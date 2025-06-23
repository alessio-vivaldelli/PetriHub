package it.petrinet.petrinet.view;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.function.BiConsumer;

import it.petrinet.model.Computation;
import it.petrinet.petrinet.builder.PetriNetBuilder;
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

  private void computeFirableTransitions() {
    if (marking == null) {
      return;
    }

    getGraphVertices().stream().forEach(vertex -> {
      addNodeStyle(vertex, "firable");
      if (vertex.element() instanceof Transition t) {
        if (incidentEdges(vertex).stream()
            .map(edge -> (edge.vertices()[0].equals(vertex) ? edge.vertices()[1] : edge.vertices()[0]))
            .allMatch(node -> ((Place) node.element()).getPlaceTokens() > 0)) {
          System.out.println("TRANSITION " + t + " is FIRABLE");
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
    });
  }

}
