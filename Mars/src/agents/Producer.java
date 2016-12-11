package agents;

import jade.core.AID;

import java.awt.Color;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Point;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.Vector;
import main.MarsModel;
import sajas.core.Agent;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.proto.ContractNetInitiator;
import sajas.proto.ContractNetResponder;

/**
 *
 * @author diogo
 */
public class Producer extends MarsAgent {
    
    private final Queue<Point> movementPlan;
    private final Queue<Mineral> mineralPlan;
    private AID[] otherTransporters;
    private Point lastPlannedPosition;
    private boolean done = false;
    
    public Producer(MarsModel model) {
        super(Color.GREEN, model);
        this.movementPlan = new LinkedList<>();
        this.mineralPlan = new LinkedList<>();
    }
    
    @Override
    public void setup() {
        this.lastPlannedPosition = this.getPosition();
        this.otherTransporters = this.getAgents(MarsAgent.Ontologies.TRANSPORTER);
        this.addBehaviour(new RoutineBehaviour());
        this.addBehaviour(new AnswerCallBehaviour());
        this.model.registerOnNoMoreMinerals(() -> this.scheduleRetreat());
    }
    
    public void scheduleRetreat() {
        Queue<Point> retreatPlan = this.getPlanToPosition(lastPlannedPosition, this.model.shipPosition, 0);
        this.movementPlan.addAll(retreatPlan);
        this.done = true;
    }
    
    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if(done) {
                if(Producer.this.getPosition().distance(model.shipPosition) <= 0)
                    removeBehaviour(this);
                else {
                    Point nextMove = Producer.this.movementPlan.poll();
                    if(nextMove != null)
                        Producer.this.translate(nextMove);
                }
            }
            
            Mineral nextMineral = Producer.this.mineralPlan.peek();
            if(nextMineral == null)
                return;
            
            Point position = Producer.this.getPosition();
            Point mineralPosition = nextMineral.getPosition();
            if(Math.abs(position.distance(mineralPosition)) <= 1) {
                Producer.this.mineralPlan.poll();
                MineralFragments fragments = nextMineral.mine();
                
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                msg.setContent(mineralPosition.x + "," + mineralPosition.y + "," + fragments.quantity.get());
                for(AID aid : otherTransporters)
                    msg.addReceiver(aid);
                
                Producer.this.addBehaviour(new RequestTransporterBehaviour(fragments, msg));
            } else {
                Point nextMove = Producer.this.movementPlan.poll();
                if(nextMove != null)
                    Producer.this.translate(nextMove);
            }
        }
        
    }
    
    private class AnswerCallBehaviour extends ContractNetResponder {
        
        public AnswerCallBehaviour() {
            super(Producer.this, MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET), 
                    MessageTemplate.MatchOntology(MarsAgent.Ontologies.SPOTTER)));
        }

        @Override
        public ACLMessage handleCfp(ACLMessage message) {
            ACLMessage response = message.createReply();
            response.setPerformative(ACLMessage.PROPOSE);
            
            int cost = movementPlan.size();
            String[] coordinates = message.getContent().split(",");
            Point mineralPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
                    
            cost += Math.abs(mineralPosition.x - lastPlannedPosition.x);
            cost += Math.abs(mineralPosition.y - lastPlannedPosition.y);
            cost -= 1; // Because it only needs to be adjacent, not in it
            
            response.setContent("" + cost);
            return response;
        }
        
        @Override
        public ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            String[] coordinates = cfp.getContent().split(",");
            
            Point position = Producer.this.getPosition();
            Point mineralPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
            // This will keep up-to-date
            Producer.this.lastPlannedPosition = position;
            while(Math.abs(position.distance(mineralPosition)) > 1) {
                int dx = mineralPosition.x - position.x;
                int dy = mineralPosition.y - position.y;
                Point nextMove;
                if(dx != 0) {
                    dx = dx > 0 ? Math.min(1, dx) : Math.max(-1, dx);
                    nextMove = new Point(dx, 0);
                } else {
                    dy = dy > 0 ? Math.min(1, dy) : Math.max(-1, dy);
                    nextMove = new Point(0, dy);
                }
                
                Producer.this.movementPlan.add(nextMove);
                position.translate(nextMove.x, nextMove.y);
            }
            
            Mineral mineral = this.getMineralAt(mineralPosition);
            Producer.this.mineralPlan.add(mineral);
            
            ACLMessage response = accept.createReply();
            response.setPerformative(ACLMessage.INFORM);
            return response;
        }
        
        private Mineral getMineralAt(Point position) {
            Mineral found = null;
            Set<MarsAgent> agents = Producer.this.model.getAgentsAt(position);
            for(MarsAgent agent : agents) {
                if(agent instanceof Mineral) {
                    found = (Mineral)agent;
                    break;
                }
            }
            
            return found;
        }
    }
    
    private class RequestTransporterBehaviour extends ContractNetInitiator {
        
        private final MineralFragments fragments;
        private int fragmentsRemaining;
        private ACLMessage initialMessage;
        
        public RequestTransporterBehaviour(MineralFragments fragments, ACLMessage msg) {
            super(Producer.this, msg);
            this.fragments = fragments;
            this.fragmentsRemaining = fragments.quantity.get();
            this.initialMessage = msg;
        }
        
        @Override
        public void handleAllResponses(Vector proposes, Vector responses) {
            Hashtable<Integer, ACLMessage> loads = new Hashtable<>();
            
            for(Object proposeObj : proposes) {
                ACLMessage propose = (ACLMessage)proposeObj;
                String[] contents = propose.getContent().split("-");
                loads.put(Integer.parseInt(contents[0]), propose);
            }
            
            Integer[] orderedLoads = loads.keySet().toArray(new Integer[0]);
            Arrays.sort(orderedLoads);
            
            Queue<ACLMessage> chosen = new LinkedList<>();
            for(Integer load : orderedLoads) {
                if(load >= fragmentsRemaining) {
                    fragmentsRemaining = 0;
                    ACLMessage sender = loads.get(load);
                    chosen.add(sender);
                }
            }
            
            for(int i = orderedLoads.length - 1; i >= 0; i--) {
                if(fragmentsRemaining == 0)
                    break;
                
                Integer load = orderedLoads[i];
                fragmentsRemaining -= load;
                ACLMessage proposal = loads.get(load);
                chosen.add(proposal);
            }
            
            for(ACLMessage m : chosen) {
                ACLMessage response = m.createReply();
                response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                responses.add(response);
            }
            
            proposes.removeAll(chosen);
            for(Object obj : proposes) {
                ACLMessage propose = (ACLMessage)obj;
                ACLMessage response = propose.createReply();
                response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                responses.add(response);
            }
            
            if(chosen.isEmpty())
                this.reset();
        }
        
        @Override
        public void handleAllResultNotifications(Vector v) {
            for(Object obj : v) {
                ACLMessage message = (ACLMessage)obj;
                if(message.getPerformative() == ACLMessage.FAILURE) {
                    this.reset();
                    return;
                }
            }
            
            if(fragmentsRemaining > 0) {
                String[] content = initialMessage.getContent().split(",");
                content[2] = fragmentsRemaining + "";
                initialMessage.setContent(content[0] + "," + content[1] + "," + content[2]);
                this.reset(initialMessage);
            }
        }
        
    }
}
