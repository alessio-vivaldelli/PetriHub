package com.brunomnsilva.smartgraph.graphview;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;

public class ShapeSVG implements ShapeWithRadius<SVGPath> {

    public static final double SCALE = 0.05;
    protected final DoubleProperty centerX, centerY;
    protected final DoubleProperty radius;

//    protected final Polygon surrogate;
    protected final SVGPath svgPath;

    /**
     * Creates a regular polygon shape with <code>numberSides</code>
     * @param x the x-center coordinate
     * @param y the y-center coordinate
     * @param radius the radius of the enclosed circle
     */
    public ShapeSVG(double x, double y, double radius) {
        Args.requireNonNegative(x, "x");
        Args.requireNonNegative(y, "y");
        Args.requireNonNegative(radius, "radius");

        this.svgPath = new SVGPath();


//        this.surrogate = new Polygon();


        this.centerX = new SimpleDoubleProperty(x);
        this.centerY = new SimpleDoubleProperty(y);

        this.centerX.addListener((observable, oldValue, newValue) -> updateSVG());
        this.centerY.addListener((observable, oldValue, newValue) -> updateSVG());

        this.radius = new SimpleDoubleProperty( radius );
        this.radius.addListener((observable, oldValue, newValue) -> updateSVG());



        updateSVG();
    }

    protected void updateSVG() {
        double cx = centerX.doubleValue();
        double cy = centerY.doubleValue();
        double radius = getRadius();

        svgPath.setContent(
                // Cerchio interno
                "M -10 -5 " +
                        "A 5 5 0 1 0 -10 5 " +
                        "A 5 5 0 1 0 -10 -5 Z " +

                        // Cerchio piccolo destro (raggio 5, centrato a x = +10)
                        "M 10 -5 " +
                        "A 5 5 0 1 0 10 5 " +
                        "A 5 5 0 1 0 10 -5 Z " +

                        // Cerchio grande esterno (raggio 25, centrato in 0,0)
                        "M 0 -25 " +
                        "A 25 25 0 1 0 0 25 " +
                        "A 25 25 0 1 0 0 -25 Z"
        );


        svgPath.setScaleX(radius*SCALE);
        svgPath.setScaleY(radius*SCALE);

        svgPath.setTranslateX(cx);
        svgPath.setTranslateY(cy);
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
