package it.petrinet.petrinet.builder;

import it.petrinet.petrinet.IllegalConnectionException;
import it.petrinet.petrinet.model.*;
import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.List;

public class PetriNetBuilder {

  private List<Place> places;
  private List<Transition> transitions;
  private List<Arc> arcs;

  private Place startNode = null;

  public Place getStartNode() {
    return startNode;
  }

  private Place finishNode = null;

  public Place getFinishNode() {
    return finishNode;
  }

  private String petriName;

  public PetriNetBuilder(String petriNetName) {
    this.places = new ArrayList<>();
    this.transitions = new ArrayList<>();
    this.arcs = new ArrayList<>();
    this.petriName = petriNetName;
  }

  public PetriNetBuilder() {
    this("");
  }

  public PetriNetBuilder setPetriName(String name) {
    this.petriName = name;
    return this;
  }

  public PetriNetBuilder setNodePosition(String name, double x, double y) {
    Node node = getNodeByName(name);
    if (node instanceof Place place) {
      place.setPosition(new Point2D(x, y));
    } else if (node instanceof Transition transition) {
      transition.setPosition(new Point2D(x, y));
    }
    return this;
  }

  public PetriNetBuilder removeNode(String name) {
    Node node = getNodeByName(name);
    if (node instanceof Place place) {
      return removePlace(place);
    } else if (node instanceof Transition transition) {
      return removeTransition(transition);
    }
    return this;
  }

  private PetriNetBuilder removePlace(Place place) {
    if (place.equals(startNode)) {
      startNode = null;
    }
    if (place.equals(finishNode)) {
      finishNode = null;
    }
    System.out.println("Removing place: " + places.remove(place));
    return this;
  }

  private PetriNetBuilder removeTransition(Transition transition) {
    System.out.println("Removing transition: " + transitions.remove(transition));
    return this;
  }

  public PetriNetBuilder removeArc(String from, String to) {
    Arc arc = new Arc(from, to);
    arcs.remove(arc);
    return this;
  }

  public PlaceBuilder newPlace(String name) {
    return new PlaceBuilder(this, name);
  }

  public PlaceBuilder newPlace(Place place) {
    return new PlaceBuilder(this, place);
  }

  public TransitionBuilder newTransition(String name) {
    return new TransitionBuilder(this, name);
  }

  public TransitionBuilder newTransition(Transition transition) {
    return new TransitionBuilder(this, transition);
  }

  public PetriNetBuilder addArc(String from, String to) {
    Arc arc = new Arc(from, to);
    arcs.add(arc);
    return this;
  }

  public PetriNetBuilder setFinishNode(String name) {
    System.out.println("All nodes: " + places.size() + " places, " + transitions.size() + " transitions");
    Node d = getNodeByName(name);
    if (d instanceof Place p) {
      p.setType(PLACE_TYPE.END);
      if (finishNode != null) {
        finishNode.setType(PLACE_TYPE.NORMAL);
      }
      finishNode = p;
    } else if (finishNode != null && name == null) {
      finishNode.setType(PLACE_TYPE.NORMAL);
      finishNode = null;
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
    } else if (startNode != null && name == null) {
      startNode.setType(PLACE_TYPE.NORMAL);
      startNode = null;
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

    public PlaceBuilder(PetriNetBuilder petriNet, Place place) {
      this.petriNetBuilder = petriNet;
      this.place = place;
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

    public TransitionBuilder(PetriNetBuilder petriNet, Transition transition) {
      this.petriNetBuilder = petriNet;
      this.transition = transition;
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

  public Node getNodeByName(String name) {
    for (Place place : places) {
      System.out.println("Checking place: " + place.getName());
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
