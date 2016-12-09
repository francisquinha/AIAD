package main;

import agents.MarsAgent;

import java.awt.geom.Point2D;

import static java.lang.Math.abs;

/**
 * Created by Angie.
 */
public class Movement {

    private final Point2D.Double finalPosition;
    private int steps;
    private final double incX;
    private final double incY;


    Movement(Point2D.Double initialPosition, Point2D.Double finalPosition) {
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

    public void move(MarsAgent agent) {
        steps--;
        agent.node.setX(agent.node.getX() + incX);
        agent.node.setY(agent.node.getY() + incY);
        if (steps == 0) {
            agent.node.setX(finalPosition.getX());
            agent.node.setY(finalPosition.getY());
        }
    }

}
