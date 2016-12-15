package agents;

import main.Environment;
import main.MarsModel;
import sajas.core.behaviours.Behaviour;
import uchicago.src.sim.gui.OvalNetworkItem;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by Angie.
 */
public class MovingAgent extends MarsAgent {

    private final Queue<Point> movementPlan;
    private final Point lastPlannedPosition;
    private boolean done;
    private int steps;
    private int lazy;

    MovingAgent(Color color, MarsModel model) {
        super(color, model, new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        movementPlan = new LinkedList<>();
        lastPlannedPosition = getPosition();
        done = false;
        steps = 0;
        lazy = 0;
    }

    void moveMovementPlan() {
        Point nextMove = movementPlan.poll();
        if (nextMove != null) {
            translate(nextMove);
            steps++;
        }
        else lazy++;
    }

    void scheduleRetreat() {
        movementPlan.addAll(getPlanToPosition(lastPlannedPosition, Environment.SHIP_POSITION, 0));
        done = true;
    }

    boolean getDone() {
        return done;
    }

    void removeBehaviourOnDone(Behaviour behaviour) {
        if (getDone()) {
            if (getPosition().distance(Environment.SHIP_POSITION) <= 0) {
                System.out.printf("%s done - %d steps - %d lazy\n", getLocalName(), steps, lazy);
                removeBehaviour(behaviour);
                model.removeAgent(this);
            }
        }
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

    void incLazy() {
        lazy++;
    }

}

