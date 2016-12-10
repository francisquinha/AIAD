package agents;

import jade.core.AID;

import java.awt.Color;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import main.MarsModel;
import sajas.core.behaviours.CyclicBehaviour;
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
    }
    
    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            Mineral nextMineral = Producer.this.mineralPlan.peek();
            if(nextMineral == null)
                return;
            
            Point position = Producer.this.getPosition();
            Point mineralPosition = nextMineral.getPosition();
            if(Math.abs(position.distance(mineralPosition)) <= 1) {
                Producer.this.mineralPlan.poll();
                nextMineral.mine();
            } else {
                Point nextMove = Producer.this.movementPlan.poll();
                if(nextMove != null)
                    Producer.this.translate(nextMove);
            }
        }
        
    }
    
    private class AnswerCallBehaviour extends ContractNetResponder {
        
        public AnswerCallBehaviour() {
            super(Producer.this, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET));
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
}
