package it.petrinet.petrinet.model;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;
import com.brunomnsilva.smartgraph.graphview.SmartShapeTypeSource;
import javafx.geometry.Point2D;

public abstract class Node {

  private String name;
  private Point2D position;

  /**
   * Set node name, used as well for label
   * 
   * @param name
   * @param position
   */
  public Node(String name, Point2D position) {
    if (name.isBlank()) {
      throw new IllegalArgumentException("Name can't be empty");
    }
    this.name = name;
    this.position = position;
  }

  /**
   * @param name
   */
  public Node(String name) {
    this(name, new Point2D(0, 0));
  }

  /**
   * Sets the name of this place.
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return
   */
  public Point2D getPosition() {
    return position;
  }

  /**
   * Update the position of the Node
   *
   * @param position
   */
  public void updatePosition(Point2D position) {
    this.position = position;
  }

  /**
   * Update the position of the Node
   *
   * @param position
   */
  public void setPosition(Point2D position) {
    this.position = position;
  }

  /**
   * Returns the label of the node.
   * 
   * @return the name of the node
   */
  @SmartLabelSource
  final public String getName() {
    return this.name;
  }

  /*
   * Establishes the shape of the vertex to use when representing the net.
   * 
   * @return the name of the shape, see {@link
   * com.brunomnsilva.smartgraph.graphview.ShapeFactory}
   */
  @SmartShapeTypeSource
  public abstract String modelShape();

  // Additional method for compatibility with CustomVertex
  public String getShapeType() {
    return modelShape();
  }
}
