package com.brunomnsilva.smartgraph.graphview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ShapeSVG implements ShapeWithRadius<SVGPath> {

  public static final double SCALE = 0.05;
  protected final DoubleProperty centerX, centerY;
  protected final DoubleProperty radius;

  // protected final Polygon surrogate;
  protected final SVGPath svgPath;

  /**
   * Creates a regular polygon shape with <code>numberSides</code>
   * 
   * @param x      the x-center coordinate
   * @param y      the y-center coordinate
   * @param radius the radius of the enclosed circle
   */
  public ShapeSVG(double x, double y, double radius) {
    Args.requireNonNegative(x, "x");
    Args.requireNonNegative(y, "y");
    Args.requireNonNegative(radius, "radius");

    this.svgPath = new SVGPath();

    // this.surrogate = new Polygon();

    this.centerX = new SimpleDoubleProperty(x);
    this.centerY = new SimpleDoubleProperty(y);

    this.centerX.addListener((observable, oldValue, newValue) -> updateSVG());
    this.centerY.addListener((observable, oldValue, newValue) -> updateSVG());

    this.radius = new SimpleDoubleProperty(radius);
    this.radius.addListener((observable, oldValue, newValue) -> updateSVG());

    updateSVG();
  }

  protected void updateSVG() {
    double cx = centerX.doubleValue();
    double cy = centerY.doubleValue();
    double radius = getRadius();

    String path = String.join("", generatePackedCircles(radius, radius * 0.1, radius * 0.5, 10));

    svgPath.setContent(
        // // Cerchio interno
        // "M -10 -5 " +
        // "A 5 5 0 1 0 -10 5 " +
        // "A 5 5 0 1 0 -10 -5 Z " +

        path +

        // Cerchio grande esterno (raggio 25, centrato in 0,0)
            "M 0 -25 " +
            "A 25 25 0 1 0 0 25 " +
            "A 25 25 0 1 0 0 -25 Z");

    svgPath.setScaleX(radius * SCALE);
    svgPath.setScaleY(radius * SCALE);

    svgPath.setTranslateX(cx);
    svgPath.setTranslateY(cy);
  }

  // Trova il massimo raggio possibile
  public static double findMaxRadius(double R, double rMin, double rMax, int targetCount) {
    double eps = 1e-6;
    double low = rMin, high = rMax, best = rMin;

    while (high - low > eps) {
      double mid = (low + high) / 2.0;
      int count = (int) Math.floor(Math.PI * (R - mid) / mid);

      if (count >= targetCount) {
        best = mid;
        low = mid;
      } else {
        high = mid;
      }
    }

    return best;
  }

  public static List<String> generatePackedCircles(double R, double rMin, double rMax, int count) {
    List<String> paths = new ArrayList<>();

    double radius = findMaxRadius(R, rMin, rMax, count);
    int numCircles = (int) Math.floor(Math.PI * (R - radius) / radius);
    numCircles = Math.min(numCircles, count);

    double circleRadius = R - radius; // raggio della circonferenza su cui piazzare i cerchi

    for (int i = 0; i < numCircles; i++) {
      double angle = 2 * Math.PI * i / numCircles;
      double x = circleRadius * Math.cos(angle);
      double y = circleRadius * Math.sin(angle);
      paths.add(svgCirclePath(x, y, radius));
    }

    return paths;
  }

  private static String svgCirclePath(double x, double y, double radius) {
    return String.format(
        "M %.2f %.2f " + // Move to top of the circle
            "A %.2f %.2f 0 1 0 %.2f %.2f " + // First arc
            "A %.2f %.2f 0 1 0 %.2f %.2f Z", // Second arc
        x, y - radius, // Move to (x, y - r)
        radius, radius, x, y + radius, // First arc to (x, y + r)
        radius, radius, x, y - radius // Second arc back to start
    );
  }

  @Override
  public Shape getShape() {
    return this.svgPath;
  }

  @Override
  public DoubleProperty centerXProperty() {
    return this.centerX;
  }

  @Override
  public DoubleProperty centerYProperty() {
    return this.centerY;
  }

  @Override
  public DoubleProperty radiusProperty() {
    return this.radius;
  }

  @Override
  public double getRadius() {
    return this.radius.doubleValue();
  }

  @Override
  public void setRadius(double radius) {
    Args.requireNonNegative(radius, "radius");

    this.radius.set(radius);
  }
}
