package it.petrinet.petrinet.builder;

import java.util.ArrayList;
import java.util.List;

import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.model.*;
import javafx.geometry.Point2D;

public class PetriNetBuilder {

  private List<Place> places;
  private List<Transition> transitions;
  private List<Arc> arcs;

  private Place startNode;
  private Place finishNode;
  private String petriName;

  public PetriNetBuilder(String petriNetName) {
    this.places = new ArrayList<>();
    this.transitions = new ArrayList<>();
    this.arcs = new ArrayList<>();
    this.petriName = petriNetName;
  }

  public PlaceBuilder newPlace(String name) {
    return new PlaceBuilder(this, name);
  }

  public TransitionBuilder newTransition(String name) {
    return new TransitionBuilder(this, name);
  }

  public PetriNetBuilder addArc(String from, String to) {
    Arc arc = new Arc(from, to);
    arcs.add(arc);
    return this;
  }

  public PetriNetBuilder setFinishNode(String name) {
    Node d = getNodeByName(name);
    if (d instanceof Place p) {
      p.setType(PLACE_TYPE.END);
      if (finishNode != null) {
        finishNode.setType(PLACE_TYPE.NORMAL);
      }
      finishNode = p;
    }
    return this;
  }

  public PetriNetBuilder setStartNode(String name) {
    Node d = getNodeByName(name);
    if (d instanceof Place p) {
      p.setType(PLACE_TYPE.START);
      if (startNode != null) {
        startNode.setType(PLACE_TYPE.NORMAL);
      }
      startNode = p;
    }
    return this;
  }

  public PetriNetModel build() throws IllegalConnectionException {
    if (startNode == null) {
      for (Place place : places) {
        if (place.getType() == PLACE_TYPE.START) {
          startNode = place;
          break;
        }
      }
    }
    if (finishNode == null) {
      for (Place place : places) {
        if (place.getType() == PLACE_TYPE.END) {
          finishNode = place;
          break;
        }
      }
    }
    // Print build information
    System.out.println("Building Petri Net: " + petriName);
    System.out.println("Places: " + places.size());
    System.out.println("Transitions: " + transitions.size());
    System.out.println("Arcs: " + arcs.size());
    System.out.println("Start Node: " + (startNode != null ? startNode.getName() : "None"));
    System.out.println("Finish Node: " + (finishNode != null ? finishNode.getName() : "None"));

    if (places == null || transitions == null) {
      throw new IllegalStateException("One or more required fields are null");
    }
    return new PetriNetModel(petriName, places, transitions, arcs, startNode, finishNode);
  }

  public class PlaceBuilder {
    private PetriNetBuilder petriNetBuilder;
    private Place place;

    public PlaceBuilder(PetriNetBuilder petriNetBuilder, String name) {
      this.petriNetBuilder = petriNetBuilder;
      this.place = new Place(name);
    }

    public PlaceBuilder initialMarking(int count) {
      place.setPlaceTokens(count);
      return this;
    }

    public PlaceBuilder withType(PLACE_TYPE type) {
      place.setType(type);
      return this;
    }

    public PlaceBuilder withPosition(double x, double y) {
      place.setPosition(new Point2D(x, y));
      return this;
    }

    public PetriNetBuilder donePlace() {
      petriNetBuilder.places.add(place);
      return petriNetBuilder;
    }
  }

  public class TransitionBuilder {
    private PetriNetBuilder petriNetBuilder;
    private Transition transition;

    public TransitionBuilder(PetriNetBuilder petriNetBuilder, String name) {
      this.petriNetBuilder = petriNetBuilder;
      this.transition = new Transition(name);
    }

    public TransitionBuilder withType(TRANSITION_TYPE type) {
      transition.setType(type);
      return this;
    }

    public TransitionBuilder withPosition(double x, double y) {
      transition.setPosition(new Point2D(x, y));
      return this;
    }

    public PetriNetBuilder doneTransition() {
      petriNetBuilder.transitions.add(transition);
      return petriNetBuilder;
    }
  }

  private Node getNodeByName(String name) {
    for (Place place : places) {
      if (place.getName().equals(name)) {
        return place;
      }
    }
    for (Transition transition : transitions) {
      if (transition.getName().equals(name)) {
        return transition;
      }
    }
    return null;
  }

}
