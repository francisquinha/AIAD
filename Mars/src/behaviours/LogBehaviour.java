package behaviours;


import jade.lang.acl.ACLMessage;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;


/**
 * Simple example behavior.
 * 
 * @author diogo
 */
public class LogBehaviour extends CyclicBehaviour {

    private Agent agent;
    
    public LogBehaviour(Agent agent) {
        this.agent = agent;
    }
    
    @Override
    public void action() {
        ACLMessage message = agent.receive();
        if(message != null) {
            System.out.println("LOG: " + message.getContent());
        }
    }
    
}
