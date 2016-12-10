package agents;

import jade.core.AID;

import java.awt.Color;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;
import main.MarsModel;
import sajas.proto.ContractNetResponder;

/**
 *
 * @author diogo
 */
public class Producer extends MarsAgent {
    
    private final Queue<Point> movementPlan;
    private AID[] otherTransporters;
    private Point lastPlannedPosition;
    
    public Producer(MarsModel model) {
        super(Color.GREEN, model);
        this.movementPlan = new LinkedList<>();
    }
    
    @Override
    public void setup() {
        this.lastPlannedPosition = this.getPosition();
        this.otherTransporters = this.getAgents(MarsAgent.Ontologies.TRANSPORTER);
        this.addBehaviour(new AnswerCallBehaviour());
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
            ACLMessage response = accept.createReply();
            response.setPerformative(ACLMessage.INFORM);
            
            return response;
        }
    }
}
