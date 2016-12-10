package agents;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Color;
import java.awt.Point;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
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
    private AID[] otherProducers;
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
        this.otherProducers = Spotter.this.getAgents(MarsAgent.Ontologies.PRODUCER);
        
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
                Spotter.this.rowYOffset = this.yOffset;
                Spotter.this.rowHeight = this.height;
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
        private final Queue<Point> movementPlan = new LinkedList<>();
        
        public ScanBehaviour() {
            Point position = new Point((int)Spotter.this.node.getX(), (int)Spotter.this.rowYOffset);
            int maxX = Environment.SIZE;
            int maxY = Environment.SIZE;
            
            Point down = new Point(0, 1);
            Point left = new Point(-1, 0);
            Point right = new Point(1, 0);

            int targetX = maxX;
            int targetY = Spotter.this.rowYOffset + Spotter.this.rowHeight - 1;

            int xVector = 1;
            while(position.x != targetX || position.y != targetY) {
                Point nextMove;
                if(xVector == 1) {
                    if(position.x >= maxX) {
                        nextMove = down;
                        xVector = -1;
                    } else
                        nextMove = right;
                } else {
                    if(position.x <= 0) {
                        nextMove = down;
                        xVector = 1;
                    } else
                        nextMove = left;
                }
                
                this.movementPlan.offer(nextMove);
                position.x += nextMove.x;
                position.y += nextMove.y;
            }
            
            this.inPosition = (int)Spotter.this.node.getY() >= Spotter.this.rowYOffset;
        }
        
        @Override
        public void action() {           
            if(!inPosition) {
                int nextY = (int)Spotter.this.node.getY() + 1;
                Spotter.this.node.setY(nextY);
                if(nextY >= Spotter.this.rowYOffset)
                    inPosition = true;
                    
                return;
            }
            
            Point nextMove = this.movementPlan.poll();
            if(nextMove == null) {
                System.out.println(Spotter.this.localName + " finished scanning at (" + Spotter.this.node.getX() + ", " + Spotter.this.node.getY() + ")");
                Spotter.this.removeBehaviour(this);
            }
            else {
                int nextX = (int)Spotter.this.node.getX() + nextMove.x;
                int nextY = (int)Spotter.this.node.getY() + nextMove.y;
                
                Spotter.this.node.setX(nextX);
                Spotter.this.node.setY(nextY);
            }
        }
    }
}
