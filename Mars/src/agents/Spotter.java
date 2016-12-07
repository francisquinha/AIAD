package agents;


import behaviours.AnswerAreaRequestBehaviour;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Color;
import sajas.proto.AchieveREInitiator;
import sajas.proto.AchieveREResponder;

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
        MessageTemplate template = AchieveREResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_REQUEST);
        this.addBehaviour(new AnswerAreaRequestBehaviour(this, template));
    }
    
    public void assignRow(int yOffset, int height) {
        this.rowYOffset = yOffset;
        this.rowHeight = height;
        this.node.setX(0);
        this.node.setY(yOffset);
        System.out.println(this.getAID().getLocalName() + " set to Y:" + yOffset);
        
        ACLMessage message = new ACLMessage(ACLMessage.REQUEST);
        message.setContent(yOffset + "-" + height);
        message.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        
        AID[] aids = this.getAgents(MarsAgent.Ontologies.SPOTTER);
        String localName = this.getAID().getLocalName();
        for(AID aid : aids) {
            if(aid.getLocalName().equals(localName))
                continue;
            
            message.addReceiver(aid);
        }
        
        this.addBehaviour(new AchieveREInitiator(this, message));
    }
    
}
