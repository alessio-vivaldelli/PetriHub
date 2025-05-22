package it.petrinet.petrinet;

import javafx.geometry.Point2D;

/**
 * Represents a place (node) in a Petri net.
 * A Place can hold tokens and has a specific type (e.g., START, END, or
 * regular).
 * It provides methods to query its type and the number of tokens it contains.
 */
public class Place extends Node {
  public static String PLACE_SHAPE = "place";

  private PLACE_TYPE type;
  private int tokens;

  /**
   * Constructs a Place with the specified name, position, type, and initial
   * tokens.
   *
   * @param name     the name of the place
   * @param position the position of the place in 2D space
   * @param type     the type of the place (e.g., START, FINISH)
   * @param tokens   the initial number of tokens in the place
   */
  public Place(String name, Point2D position, PLACE_TYPE type, int tokens) {
    super(name, position);
    this.type = type;
    this.tokens = tokens;
  }

  /**
   * Constructs a Place with the specified name, position, and type.
   * The initial number of tokens is set to 0.
   *
   * @param name     the name of the place
   * @param position the position of the place in 2D space
   * @param type     the type of the place (e.g., START, FINISH)
   */
  public Place(String name, Point2D position, PLACE_TYPE type) {
    this(name, position, type, 0);
  }

  /**
   * Constructs a Place with the specified name and position.
   * The type is set to null and the initial number of tokens is set to 0.
   *
   * @param name     the name of the place
   * @param position the position of the place in 2D space
   */
  public Place(String name, Point2D position) {
    this(name, position, null, 0);
  }

  @Override
  public String modelShape() {
    return "%s_%d".formatted(PLACE_SHAPE, getPlaceTokens());
  }

  /**
   * Returns the current number of tokens in this place.
   *
   * @return the number of tokens
   */
  public int getPlaceTokens() {
    return tokens;
  }

  /**
   * Checks if this place is a start place.
   *
   * @return true if this place is of type START, false otherwise
   */
  public boolean isStartPlace() {
    return type == PLACE_TYPE.START;
  }

  /**
   * Checks if this place is an end place.
   *
   * @return true if this place is of type END, false otherwise
   */
  public boolean isEndPlace() {
    return type == PLACE_TYPE.END;
  }
}
