package behaviours;

import agents.Transporter;
import main.Movement;
import main.TransportMovement;
import sajas.core.behaviours.CyclicBehaviour;

/**
 * Created by Angie.
 */
public class TransporterMoveBehaviour extends CyclicBehaviour {

    private final Transporter transporter;

    public TransporterMoveBehaviour(Transporter transporter) {
        this.transporter = transporter;
    }

    @Override
    public void action() {
        TransportMovement transport = transporter.getCurrentTransport();
        if (transport == null)
            transporter.getNextTransport();
        else {
            Movement movement = transport.getMovement2Place();
            if (movement.getSteps() > 0) {
                movement.move(transporter);
            }
            else {
                movement = transport.getMovement2Ship();
                if (movement.getSteps() == 0) {
                    transporter.getNextTransport();
                }
                else {
                    movement.move(transporter);
                }
            }

        }

    }

}
