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

  public PetriNetBuilder(String petriNetName) {
    this.places = new ArrayList<>();
    this.transitions = new ArrayList<>();
    this.arcs = new ArrayList<>();
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

  public PetriNetModel build() throws IllegalConnectionException {
    return new PetriNetModel(places, transitions, arcs);
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
