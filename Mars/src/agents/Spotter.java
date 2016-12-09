package agents;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Color;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import main.Environment;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.proto.ProposeInitiator;
import sajas.proto.ProposeResponder;
/**
 *
 * @author diogo
 */
public class Spotter extends MarsAgent {
    
    private AID[] otherSpotters;
    private String localName;
    private int rowYOffset;
    private int rowHeight;
    
    private final HashMap<String, AID> areaOwners = new HashMap<>();
    private final HashMap<String, AID> areaNegotiations = new HashMap<>();
    
    public Spotter() {
        super(Color.RED);
    }
    
    @Override
    protected void setup() {
        this.localName = this.getLocalName();
        AID[] allSpotters = Spotter.this.getAgents(MarsAgent.Ontologies.SPOTTER);
        this.otherSpotters = Arrays.stream(allSpotters)
                                    .filter((aid) -> !aid.getLocalName().equals(localName))
                                    .toArray(AID[]::new);
        
        this.addBehaviour(new AnswerAreaRequestBehaviour());
        this.addBehaviour(new AcknowledgeAreaBehaviour());
    }
    
    public void assignRow(int yOffset, int height) {
        this.addBehaviour(new RequestAreaBehaviour(yOffset, height));
    }
    
    private class RequestAreaBehaviour extends ProposeInitiator {

        private final int yOffset;
        private final int height;
        private final Set<String> awaitingConfirmation;
        
        public RequestAreaBehaviour(int yOffset, int height) {
            super(Spotter.this, null);
            this.yOffset = yOffset;
            this.height = height;
            this.awaitingConfirmation = new HashSet<>();
        }

        @Override
        @SuppressWarnings("UseOfObsoleteCollectionType")
        protected Vector<ACLMessage> prepareInitiations(ACLMessage initialPropose) {
            Vector<ACLMessage> messages = new Vector<>();
            ACLMessage proposeMessage = this.buildMessage();
            messages.add(proposeMessage);
            
            for(AID spotter : Spotter.this.otherSpotters)
                this.awaitingConfirmation.add(spotter.getLocalName());
            
            return messages;
        }

        @Override
        protected void handleAcceptProposal(ACLMessage response) {
            if(this.awaitingConfirmation.isEmpty())
                return;
            
            AID sender = response.getSender();
            this.awaitingConfirmation.remove(sender.getLocalName());
            
            if(this.awaitingConfirmation.isEmpty()) {
                System.out.println(Spotter.this.localName + " has now " + this.yOffset + "-" + this.height);
                Spotter.this.node.setY(this.yOffset);
                Spotter.this.node.setX(0);
                this.sendAreaConfirmation();
                Spotter.this.addBehaviour(new ScanBehaviour());
            }
        }
        
        @Override
        protected void handleRejectProposal(ACLMessage response) {
            System.out.println(Spotter.this.localName + " was rejected for " + this.yOffset + "-" + this.height);
        }
        
        public ACLMessage buildMessage() {
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.setContent(yOffset + "-" + height);
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);

            for (AID spotter : Spotter.this.otherSpotters) {
                message.addReceiver(spotter);
            }

            return message;
        }
        
        private void sendAreaConfirmation() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent(this.yOffset + "-" + this.height);
            message.setSender(Spotter.this.getAID());
            for(AID receiver : Spotter.this.otherSpotters)
                message.addReceiver(receiver);
            
            send(message);
        }
    }
    
    private class AnswerAreaRequestBehaviour extends ProposeResponder {
        
        public AnswerAreaRequestBehaviour() {
            super(Spotter.this, ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE));
        }
        
        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) {
            String area = propose.getContent();
            ACLMessage message = propose.createReply();
            
            if(Spotter.this.areaOwners.containsKey(area))
                message.setPerformative(ACLMessage.REJECT_PROPOSAL);
            else if(Spotter.this.areaNegotiations.containsKey(area))
                message.setPerformative(ACLMessage.REJECT_PROPOSAL);
            else {
                Spotter.this.areaNegotiations.put(area, propose.getSender());
                message.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }
            
            return message;
        }
    }
    
    private class AcknowledgeAreaBehaviour extends CyclicBehaviour {
        
        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if(msg == null) 
                return;
            
            AID sender = msg.getSender();
            String area = msg.getContent();
            if(Spotter.this.areaNegotiations.containsKey(area)) {
                AID registered = Spotter.this.areaNegotiations.get(area);
                if(registered.getLocalName().equals(sender.getLocalName())) {
                    Spotter.this.areaNegotiations.remove(area);
                    Spotter.this.areaOwners.put(area, sender);
                    this.reset();
                }
            }
        }
    }
    
    private class ScanBehaviour extends CyclicBehaviour {

        private boolean inPosition = false;
        
        @Override
        public void action() {
            int currentX = (int)Spotter.this.node.getX();
            int currentY = (int)Spotter.this.node.getY();
            int targetY = Spotter.this.rowYOffset;
            int maxX = Environment.SIZE * Environment.CELL_SIZE;
            
            if(!inPosition) {
                int newY = currentY + Environment.CELL_SIZE;
                Spotter.this.node.setY(newY);
                if(newY >= targetY)
                    inPosition = true;
            } else {
                int newX = currentX + Environment.CELL_SIZE;
                Spotter.this.node.setX(newX);
                if(newX >= maxX)
                    Spotter.this.removeBehaviour(this);
            }
        }
        
    }
}
