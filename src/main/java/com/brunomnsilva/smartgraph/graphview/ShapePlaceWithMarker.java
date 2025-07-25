/*
 * The MIT License
 *
 * JavaFXSmartGraph | Copyright 2024  brunomnsilva@gmail.com
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.brunomnsilva.smartgraph.graphview;

import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Shape;

/**
 * This class represents a circle shape with a specified radius.
 *
 * @author brunomnsilva
 */
public class ShapePlaceWithMarker implements ShapeWithRadius<Shape> {

  private final Circle surrogate;
  private final Circle innerCircle;
  private Shape total;
  /**
   * Creates a circle shape.
   *
   * @param x      the x-center coordinate
   * @param y      the y-center coordinate
   * @param radius the radius of the circle
   */
  public ShapePlaceWithMarker(double x, double y, double radius) {
    Args.requireNonNegative(x, "x");
    Args.requireNonNegative(y, "y");
    Args.requireNonNegative(radius, "radius");

    this.innerCircle = new Circle(x, y, radius*0.2);
    this.innerCircle.setFill(Color.BLACK);
    this.surrogate = new Circle(x, y, radius);
    this.surrogate.setFill(Color.YELLOW);

    this.total = Shape.subtract(surrogate, innerCircle);
  }

  @Override
  public Shape getShape() {
    return total;
  }

  @Override
  public DoubleProperty centerXProperty() {
      return total.translateXProperty();
  }

  @Override
  public DoubleProperty centerYProperty() {
      return total.translateYProperty();
  }

  @Override
  public DoubleProperty radiusProperty() {
      return surrogate.radiusProperty();
  }

  @Override
  public double getRadius() {
    return surrogate.getRadius();
  }

  @Override
  public void setRadius(double radius) {
    Args.requireNonNegative(radius, "radius");

    // Only update if different
    if (Double.compare(this.getRadius(), radius) != 0) {
      surrogate.setRadius(radius);
      innerCircle.setRadius(radius*0.2);
      total = Shape.union(surrogate, innerCircle);
    }
  }
}
