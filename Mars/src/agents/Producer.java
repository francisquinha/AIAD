package agents;


import behaviours.LogBehaviour;
import sajas.core.Agent;


/**
 *
 * @author diogo
 */
public class Producer extends Agent {
    
    @Override
    protected void setup() {
        this.addBehaviour(new LogBehaviour(this));
        System.out.println("A producer was set up!");
    }
    
}
