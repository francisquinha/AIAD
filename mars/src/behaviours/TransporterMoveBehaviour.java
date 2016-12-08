package behaviours;

import agents.Transporter;
import main.Movement;
import sajas.core.behaviours.CyclicBehaviour;

import java.awt.geom.Point2D;

/**
 * Created by Angie.
 */
public class TransporterMoveBehaviour extends CyclicBehaviour {

    private final Transporter transporter;
    private final Movement movement;

    public TransporterMoveBehaviour(Transporter transporter, Point2D.Double position) {
        this.transporter = transporter;
        movement = new Movement(transporter.getPosition(), position);
        System.out.printf("in = (%.2f, %.2f), out = (%.2f, %.2f), steps = %d, inc = (%.2f, %.2f)\n",
                this.transporter.node.getX(), this.transporter.node.getY(),
                position.getX(), position.getY(),
                movement.getSteps(),
                movement.getIncX(), movement.getIncY());
    }

    @Override
    public void action() {
        if (movement.getSteps() > 0) {
            movement.decSteps();
            transporter.node.setX(transporter.node.getX() + movement.getIncX());
            transporter.node.setY(transporter.node.getY() + movement.getIncY());
        }
        else {
            transporter.node.setX(movement.getFinalPosition().getX());
            transporter.node.setY(movement.getFinalPosition().getY());
        }
    }

}
