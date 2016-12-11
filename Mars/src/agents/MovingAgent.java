package agents;

import main.Environment;
import main.MarsModel;
import uchicago.src.sim.gui.OvalNetworkItem;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Angie.
 */
class MovingAgent extends MarsAgent {

    private Queue<Point> movementPlan;
    private Point lastPlannedPosition;
    private boolean done;

    MovingAgent(Color color, MarsModel model) {
        super(color, model, new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        movementPlan = new LinkedList<>();
        this.lastPlannedPosition = getPosition();
        done = false;
    }

    void moveMovementPlan() {
        Point nextMove = movementPlan.poll();
        if (nextMove != null)
            translate(nextMove);
    }

    void scheduleRetreat() {
        this.movementPlan.addAll(getPlanToPosition(lastPlannedPosition, Environment.SHIP_POSITION, 0));
        this.done = true;
    }

    boolean getDone() {
        return done;
    }

    int getPlanCost() {
        return movementPlan.size();
    }

    int getCost(Point target) {
        return Math.abs(target.x - lastPlannedPosition.x) + Math.abs(target.y - lastPlannedPosition.y);
    }

    void addMovementPlan(Point target, int maxDistance) {
        movementPlan.addAll(getPlanToPosition(lastPlannedPosition, target, maxDistance));
    }

}

