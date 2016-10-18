
import jade.core.AID;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 *
 * @author diogo
 */
public class Spotter extends Agent {
    
    @Override
    protected void setup() {
        System.out.println("A spotter was set up!");
        System.out.println("Sending a hello message!");
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.addReceiver(new AID("Transporter", AID.ISLOCALNAME));
        message.addReceiver(new AID("Spotter", AID.ISLOCALNAME));
        message.setLanguage("English");
        message.setContent("Hello!");
        send(message);
    }
    
}
