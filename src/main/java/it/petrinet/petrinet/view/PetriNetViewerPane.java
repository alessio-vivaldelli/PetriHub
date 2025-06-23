package it.petrinet.petrinet.view;

import java.io.File;
import java.io.IOException;
import java.util.function.BiConsumer;

import it.petrinet.model.Computation;
import it.petrinet.petrinet.builder.PetriNetBuilder;
import it.petrinet.petrinet.model.PetriNetModel;
import it.petrinet.petrinet.model.Place;
import it.petrinet.petrinet.model.Transition;
import it.petrinet.petrinet.persistance.pnml.PNMLParser;

public class PetriNetViewerPane extends AbstractPetriNetPane {

  private Computation computation;
  private PetriNetModel petriNetModel;
  private String petriName;
  private final String petriNetPNML;

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

    PNMLParser parser = new PNMLParser();
    try {
      this.petriNetModel = parser.parse(petriNetPNML);
    } catch (IOException e) {
      e.printStackTrace();
    }
    this.petriName = petriNetModel.getName();

    petriNetModel.getNodes().forEach(node -> {
      if (node instanceof Place p) {
        addNodeToGraph(p);
      } else if (node instanceof Transition t) {
        addNodeToGraph(t);
      }
    });

    petriNetModel.getConnections().forEach((from, toNodes) -> {
      toNodes.forEach(to -> {
        System.out.println("From: " + from.getName() + ", To: " + to.getName());
        addArcToGraph(from.getName(), to.getName());
      });
    });

  }

}
