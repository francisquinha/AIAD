package agents;

import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.Environment;
import main.MarsModel;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.proto.ContractNetResponder;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author diogo
 */
public class Transporter extends MarsAgent {

    private final int capacity;
    private int load;
    private Queue<Point> movementPlan;
    private Queue<MineralFragments> fragmentsPlan;
    private Point lastPlannedPosition;
    private int lastPlannedLoad;
    
    private Queue<ACLMessage> pending = new LinkedList<>();
    private Queue<ACLMessage> toReject = new LinkedList<>();
    
    public Transporter(MarsModel model, int capacity) {
        super(Color.BLUE, model);
        this.capacity = capacity;
        this.load = 0;
        this.lastPlannedLoad = 0;
        this.fragmentsPlan = new LinkedList<>();
        this.movementPlan = new LinkedList<>();
    }
    
    @Override
    public void setup() {
        this.lastPlannedPosition = getPosition();
        this.addBehaviour(new RoutineBehaviour());
        this.addBehaviour(new AnswerCallBehaviour());
    }
    
    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if(Math.abs(Environment.SHIP_POSITION.distance(Transporter.this.getPosition())) <= 1)
                load = 0;
            
            MineralFragments nextFragments = fragmentsPlan.peek();
            Point position = Transporter.this.getPosition();
            
            if(nextFragments != null) {
                Point mineralPosition = nextFragments.getPosition();
                if(Math.abs(position.distance(mineralPosition)) <= 1) {
                    fragmentsPlan.poll();
                    int collectable = Math.min(capacity - load, nextFragments.quantity.get());
                    nextFragments.take(collectable);
                    load += collectable;
                }
            }
            
            Point nextMove = movementPlan.poll();
            if(nextMove != null)
                Transporter.this.translate(nextMove);
        }
        
    }
    
    private class AnswerCallBehaviour extends ContractNetResponder {
        
        public AnswerCallBehaviour() {
            super(Transporter.this, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET));
        }

        @Override
        public ACLMessage handleCfp(ACLMessage message) {
            String[] content = message.getContent().split(",");
            Point fragmentsPosition = new Point(Integer.parseInt(content[0]), Integer.parseInt(content[1]));
            int totalFragments = Integer.parseInt(content[2]);
            
            int cost = movementPlan.size();
            int available = Transporter.this.capacity - Transporter.this.lastPlannedLoad;
            int collectable = Math.min(available, totalFragments);
            cost += Math.abs(fragmentsPosition.x - lastPlannedPosition.x);
            cost += Math.abs(fragmentsPosition.y - lastPlannedPosition.y);
            cost -= 1; // Because it only needs to be adjacent, not in it
            
            ACLMessage response = message.createReply();
            response.setPerformative(ACLMessage.PROPOSE);
            response.setContent(collectable + "-" + cost);
            
            pending.add(response);
            return response;
        }
        
        @Override
        public ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            if(toReject.contains(propose)) {
                ACLMessage response = accept.createReply();
                response.setPerformative(ACLMessage.FAILURE);
                toReject.remove(response);
                return response;
            }
            
            pending.remove(propose);
            toReject.addAll(pending);
            pending.clear();
            
            String[] coordinates = cfp.getContent().split(",");
            
            Point position = Transporter.this.getPosition();
            Point fragmentsPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));
            
            movementPlan.addAll(getPlanToPosition(lastPlannedPosition, fragmentsPosition, 1));

            MineralFragments fragments = this.getMineralFragmentsAt(fragmentsPosition);
            Transporter.this.fragmentsPlan.add(fragments);
            int collectable = Math.min(capacity - lastPlannedLoad, fragments.quantity.get());
            lastPlannedLoad += collectable;
            if(lastPlannedLoad >= capacity) {
                Queue<Point> planToShip = getPlanToPosition(lastPlannedPosition, Environment.SHIP_POSITION, 1);
                movementPlan.addAll(planToShip);
                planToShip.forEach(p -> lastPlannedPosition.translate(p.x, p.y));
                lastPlannedLoad = 0;
            }
            
            ACLMessage response = accept.createReply();
            response.setPerformative(ACLMessage.INFORM);
            return response;
        }
        
        @Override
        public void handleRejectProposal(ACLMessage cfp, ACLMessage proposal, ACLMessage reject) {
            toReject.remove(proposal);
        }
        
        private MineralFragments getMineralFragmentsAt(Point position) {
            MineralFragments found = null;
            Set<MarsAgent> agents = Transporter.this.model.getAgentsAt(position);
            for(MarsAgent agent : agents) {
                if(agent instanceof MineralFragments) {
                    found = (MineralFragments)agent;
                    break;
                }
            }
            
            return found;
        }
    }
    
}
