package main;

import java.awt.geom.Point2D;

import static java.lang.Math.abs;

/**
 * Created by Angie.
 */
public class Movement {

    private final Point2D.Double initialPosition;
    private final Point2D.Double finalPosition;
    private int steps;
    private final double incX;
    private final double incY;


    public Movement(Point2D.Double initialPosition, Point2D.Double finalPosition) {
        this.initialPosition = initialPosition;
        this.finalPosition = finalPosition;
        double dx = finalPosition.getX() - initialPosition.getX();
        double dy = finalPosition.getY() - initialPosition.getY();
        int absdx = (int) abs(dx);
        int absdy = (int) abs(dy);
        if (absdx > absdy) {
            steps = absdx;
        }
        else {
            steps = absdy;
        }
        if (steps == 0) {
            incX = 0;
            incY = 0;
        }
        else {
            incX = dx / steps;
            incY = dy / steps;
        }
    }

    public int getSteps() {
        return steps;
    }

    public double getIncX() {
        return incX;
    }

    public double getIncY() {
        return incY;
    }

    public void decSteps() {
        steps--;
    }

    public Point2D.Double getFinalPosition() {
        return finalPosition;
    }

}
