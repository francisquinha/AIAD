package agents;


import behaviours.SpotterScanBehaviour;
import jade.lang.acl.ACLMessage;
import java.awt.Color;
import sajas.core.AID;

/**
 *
 * @author diogo
 */
public class Spotter extends MarsAgent {
    
    private int rowYOffset;
    private int rowHeight;
    
    public Spotter() {
        super(Color.RED);
    }
    
    @Override
    protected void setup() {
        System.out.println("A spotter was set up!");
        this.addBehaviour(new SpotterScanBehaviour(this));
    }
    
    public void assignRow(int yOffset, int height) {
        this.rowYOffset = yOffset;
        this.rowHeight = height;
        this.node.setX(0);
        this.node.setY(yOffset);
        
        System.out.println("Spotter set to Y:" + yOffset);
    }
    
    public void sayHello() {
        System.out.println("Sending a hello message!");
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        
        message.addReceiver(new AID("Transporter", AID.ISLOCALNAME));
        message.addReceiver(new AID("Spotter", AID.ISLOCALNAME));
        message.setLanguage("English");
        message.setContent("Hello!");
        send(message);
    }
    
}
