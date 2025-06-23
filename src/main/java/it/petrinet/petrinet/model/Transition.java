package it.petrinet.petrinet.model;

import com.brunomnsilva.smartgraph.graphview.SmartShapeTypeSource;

import javafx.geometry.Point2D;

/**
 * Represents a transition node in a Petri net.
 * A Transition can have a specific type and a state indicating if it is
 * firable.
 * Provides methods to query its type, firability, and to get a string
 * representation for modeling.
 */
public class Transition extends Node {

  private TRANSITION_TYPE type;
  private boolean isFirable;

  /**
   * Constructs a Transition with the specified name, position, type, and firable
   * state.
   *
   * @param name      the name of the transition
   * @param position  the position of the transition in 2D space
   * @param type      the type of the transition
   * @param isFirable whether the transition is firable
   */
  public Transition(String name, Point2D position, TRANSITION_TYPE type, boolean isFirable) {
    super(name, position);
    this.type = type;
    this.isFirable = isFirable;
  }

  /**
   * Constructs a Transition with the specified name, position, and type.
   * The firable state is set to false.
   *
   * @param name     the name of the transition
   * @param position the position of the transition in 2D space
   * @param type     the type of the transition
   */
  public Transition(String name, Point2D position, TRANSITION_TYPE type) {
    this(name, position, type, false);
  }

  /**
   * Constructs a Transition with the specified name and position.
   * The type is set to USER and the firable state is set to false.
   *
   * @param name     the name of the transition
   * @param position the position of the transition in 2D space
   */
  public Transition(String name, Point2D position) {
    this(name, position, TRANSITION_TYPE.USER, false);
  }

  /**
   * Constructs a Transition with the specified name.
   * The position is set to (0, 0), the type is set to USER, and the firable
   * state is set to false.
   *
   * @param name the name of the transition
   */
  public Transition(String name) {
    this(name, new Point2D(0, 0), TRANSITION_TYPE.USER, false);
  }

  /**
   * Returns the type of this transition.
   *
   * @return the transition type
   */
  public TRANSITION_TYPE getType() {
    return type;
  }

  /**
   * Sets the type of this transition.
   *
   * @param type the new transition type
   */
  public void setType(TRANSITION_TYPE type) {
    this.type = type;
  }

  @Override
  public String toString() {
    // return "Transition [name=" + getName() + ", position=" + getPosition() + ",
    // type=" + type
    // + ", isFirable=" + isFirable + "]";
    return getName();
  }

  /**
   * Returns whether this transition is firable.
   *
   * @return true if the transition is firable, false otherwise
   */
  public void setIsFirable(boolean newValue) {
    isFirable = newValue;
  }

  /**
   * Returns whether this transition is ready to fire.
   *
   * @return true if the transition is ready to fire, false otherwise
   */
  public boolean getIsReadyToFire() {
    return isFirable;
  }

  /*
   * Establishes the shape of the vertex to use when representing the net.
   * 
   * @return the name of the shape, see {@link
   * com.brunomnsilva.smartgraph.graphview.ShapeFactory}
   */
  @SmartShapeTypeSource
  public String modelShape() {
    return "Transition";
  }
}
