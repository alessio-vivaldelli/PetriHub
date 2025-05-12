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

/**
 * This class represents a five-point star shape inscribed within a specified radius.
 *
 * @author brunomnsilva
 */
public class ShapeTransition extends ShapeRegularPolygon {

    /**
     * Creates a new star shape enclosed in a circle of <code>radius</code>.
     * @param x the x-center coordinate
     * @param y the y-center coordinate
     * @param radius the radius of the enclosed circle
     */
    public ShapeTransition(double x, double y, double radius) {
        super(x, y, radius, 4);
    }

    @Override
    protected void updatePolygon() {
        surrogate.getPoints().clear();

        double cx = centerX.doubleValue();
        double cy = centerY.doubleValue();


        double radius = getRadius();

        double up_left_x = cx - radius/2;
        double up_left_y = cy - radius/2;

        double up_right_x = cx + radius/2;
        double up_right_y = cy - radius/2;

        double down_left_x = cx - radius/2;
        double down_left_y = cy + radius;

        double down_right_x = cx + radius/2;
        double down_right_y = cy + radius;

        surrogate.getPoints().addAll(up_left_x, up_left_y,
                up_right_x, up_right_y,
                down_right_x, down_right_y,
                down_left_x, down_left_y);
    }
}
