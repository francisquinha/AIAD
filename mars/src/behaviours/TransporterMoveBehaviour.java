package behaviours;

import agents.Transporter;
import sajas.core.behaviours.CyclicBehaviour;

import java.awt.*;

import static java.lang.Math.abs;

/**
 * Created by Angie.
 */
public class TransporterMoveBehaviour extends CyclicBehaviour {

    private final Transporter transporter;
    private final Point position;
    private int steps;
    private final double incX;
    private final double incY;

    public TransporterMoveBehaviour(Transporter transporter, Point position) {
        this.transporter = transporter;
        this.position = position;
        double dx = this.position.getX() - this.transporter.node.getX();
        double dy = this.position.getY() - this.transporter.node.getY();
        int absdx = (int) abs(dx);
        int absdy = (int) abs(dy);
        if (absdx > absdy) {
            this.steps = absdx;
        }
        else {
            this.steps = absdy;
        }
        if (this.steps == 0) {
            this.incX = 0;
            this.incY = 0;
        }
        else {
            this.incX = dx / this.steps;
            this.incY = dy / this.steps;
        }

        System.out.printf("in = (%.2f, %.2f), out = (%.2f, %.2f), steps = %d, inc = (%.2f, %.2f)\n",
                this.transporter.node.getX(), this.transporter.node.getY(),
                this.position.getX(), this.position.getY(),
                this.steps,
                this.incX, this.incY);
    }

    @Override
    public void action() {
        if (this.steps > 0) {
            this.steps--;
            this.transporter.node.setX(this.transporter.node.getX() + this.incX);
            this.transporter.node.setY(this.transporter.node.getY() + this.incY);
        }
        else {
            this.transporter.node.setX(position.getX());
            this.transporter.node.setY(position.getY());
        }
    }

}
