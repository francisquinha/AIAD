package behaviours;

import agents.Producer;
import main.Movement;
import sajas.core.behaviours.CyclicBehaviour;

public class ProducerMoveBehaviour extends CyclicBehaviour{
	
    private final Producer producer;

    public ProducerMoveBehaviour(Producer producer) {
        this.producer = producer;
    }

    @Override
    public void action() {
        Movement movement = producer.getCurrentMovement();
        if (movement == null)
            producer.getNextMovement();
        else {
            if (movement.getSteps() > 0) {
            	movement.move(producer);
            }
            else {
            	producer.getNextMovement();
            }
        }
    }
}
