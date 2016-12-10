package main;

import agents.MarsAgent;

import java.awt.*;

import static java.lang.Math.abs;

/**
 * Created by Angie.
 */
public class Movement {

    private final Point finalPosition;
    private int steps;
    private final double incX;
    private final double incY;


    public Movement(Point initialPosition, Point finalPosition) {
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
