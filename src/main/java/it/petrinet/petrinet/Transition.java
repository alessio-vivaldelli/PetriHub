package it.petrinet.petrinet;

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
   * Returns a string representing the model shape of this transition.
   *
   * @return a string representing the model shape
   */
  @Override
  public String modelShape() {
    return "%s_transition".formatted(getType().toString());
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
   * Returns whether this transition is firable.
   *
   * @return true if the transition is firable, false otherwise
   */
  public boolean setIsFirable() {
    return isFirable;
  }

  /**
   * Returns whether this transition is ready to fire.
   *
   * @return true if the transition is ready to fire, false otherwise
   */
  public boolean getIsReadyToFire() {
    return isFirable;
  }
}
