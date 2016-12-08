package agents;

import behaviours.TransporterMoveBehaviour;
import main.Simulation;

import java.awt.*;


/**
 *
 * @author diogo
 */
public class Transporter extends MarsAgent {
    
    public Transporter() {
        super(Color.BLUE);
    }

    @Override
    protected void setup() {

        this.node.setX(Simulation.random.nextInt(101));
        this.node.setY(Simulation.random.nextInt(101));

        this.addBehaviour(
                new TransporterMoveBehaviour(this,
                        new Point(Simulation.random.nextInt(101), Simulation.random.nextInt(101))));
    }

}
