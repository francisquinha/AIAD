
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;


/**
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
