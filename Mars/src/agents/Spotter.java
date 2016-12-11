package agents;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.Directions;
import main.Environment;
import main.MarsModel;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.proto.ContractNetInitiator;
import sajas.proto.ProposeInitiator;
import sajas.proto.ProposeResponder;
import uchicago.src.sim.gui.OvalNetworkItem;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.Queue;

/**
 *
 * @author diogo
 */
public class Spotter extends MarsAgent {
    
    private final List<Mineral> mineralsFound;
    private AID[] otherSpotters;
    private AID[] otherProducers;
    private String localName;
    private int rowYOffset;
    private int rowHeight;

    private boolean done = false;

    private final HashMap<String, AID> areaOwners = new HashMap<>();
    private final HashMap<String, AID> areaNegotiations = new HashMap<>();
    
    public Spotter(MarsModel model) {
        super(Color.RED, model, new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        this.mineralsFound = new LinkedList<>();
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
        this.addBehaviour(new ScanBehaviour());
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
                Spotter.this.addBehaviour(new MoveBehaviour());
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

            for (AID spotter : Spotter.this.otherSpotters)
                message.addReceiver(spotter);

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
    
    private class MoveBehaviour extends CyclicBehaviour {

        private Queue<Point> movementPlan;

        public MoveBehaviour() {

            Point position;
            if (Spotter.this.rowYOffset != Spotter.this.node.getY()) {
                position = new Point(0, Spotter.this.rowYOffset);
                Point from = new Point((int) Spotter.this.node.getX(), (int) Spotter.this.node.getY());
                this.movementPlan = Spotter.this.getPlanToPosition(from, position,0);

            }
            else {
                position = new Point((int)Spotter.this.node.getX(), Spotter.this.rowYOffset);
                this.movementPlan = new LinkedList<>();
            }

            int maxX = Environment.SIZE - 1;

            int targetX = Spotter.this.rowHeight % 2 == 0 ? 0 : maxX;
            int targetY = Spotter.this.rowYOffset + Spotter.this.rowHeight - 1;

            int xVector = 1;
            while(position.x != targetX || position.y != targetY) {
                Point nextMove;
                if(xVector == 1) {
                    if(position.x >= maxX) {
                        nextMove = Directions.DOWN;
                        xVector = -1;
                    } else
                        nextMove = Directions.RIGHT;
                } else {
                    if(position.x <= 0) {
                        nextMove = Directions.DOWN;
                        xVector = 1;
                    } else
                        nextMove = Directions.LEFT;
                }
                
                this.movementPlan.offer(nextMove);
                position.x += nextMove.x;
                position.y += nextMove.y;
            }
            movementPlan.addAll(getPlanToPosition(position, Environment.SHIP_POSITION, 0));
        }
        
        @Override
        public void action() {    
            Point nextMove = this.movementPlan.poll();
            if(nextMove == null) {
                done = true;
                Spotter.this.removeBehaviour(this);
            }
            else
                Spotter.this.translate(nextMove);
        }
    }
    
    public class ScanBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if(Spotter.this.done)
                Spotter.this.removeBehaviour(this);
            
            Point newPosition = Spotter.this.getPosition();
            Set<MarsAgent> agents = Spotter.this.model.getAgentsAt(newPosition);
            for(MarsAgent agent : agents) {
                if(agent instanceof Mineral) {
                    Mineral mineral = (Mineral)agent;
                    mineral.node.setColor(Color.YELLOW);
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    msg.setOntology(MarsAgent.Ontologies.SPOTTER);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    msg.setContent(newPosition.x + "," + newPosition.y);
                    for(AID producer : Spotter.this.otherProducers)
                        msg.addReceiver(producer);
                    
                    Spotter.this.mineralsFound.add(mineral);
                    Spotter.this.addBehaviour(new RequestProducerBehaviour(mineral, msg));
                }
            }
        }
        
    }
    
    public class RequestProducerBehaviour extends ContractNetInitiator {
        
        private final Mineral mineral;
        
        public RequestProducerBehaviour(Mineral mineral, ACLMessage msg) {
            super(Spotter.this, msg);
            this.mineral = mineral;
        }
        
        @Override
        public void handleAllResponses(Vector proposes, Vector responses) {
            int minCost = Integer.MAX_VALUE;
            ACLMessage minCostProposal = null;
            
            for(Object proposeObj : proposes) {
                ACLMessage propose = (ACLMessage)proposeObj;
                int cost = Integer.parseInt(propose.getContent());
                if(cost < minCost) {
                    minCost = cost;
                    if(minCostProposal != null) {
                        ACLMessage response = minCostProposal.createReply();
                        response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                        responses.add(response);
                    }
                    
                    minCostProposal = propose;
                } else {
                    ACLMessage response = propose.createReply();
                    response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                    responses.add(response);
                }
            }
            
            if(minCostProposal != null) {
                ACLMessage selectedMessage = minCostProposal.createReply();
                selectedMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                responses.add(selectedMessage);
            }
        }
        
        @Override
        public void handleInform(ACLMessage inform) {
            System.out.println("Got a producer for a mineral!");
        }
        
    }
}
