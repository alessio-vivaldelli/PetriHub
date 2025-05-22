package it.petrinet.petrinet;

import com.brunomnsilva.smartgraph.graphview.SmartLabelSource;
import com.brunomnsilva.smartgraph.graphview.SmartShapeTypeSource;

import javafx.geometry.Point2D;

// This class is a placeholder for the Node class in the Petri net model. s
public abstract class Node {

  private final String name;
  private Point2D position;

  /**
   * Set node name, used as well for label
   * 
   * @param name
   */
  public Node(String name, Point2D position) {
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
   * Returns the name of the city.
   * 
   * @return the name of the city
   */
  @SmartLabelSource
  public String getName() {
    return this.name;
  }

  /*
   * Establishes the shape of the vertex to use when representing this city.
   * 
   * @return the name of the shape, see {@link
   * com.brunomnsilva.smartgraph.graphview.ShapeFactory}
   */
  @SmartShapeTypeSource
  public abstract String modelShape();

}
