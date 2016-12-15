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
 * @author diogo
 */
public class Transporter extends MovingAgent {

    private final int capacity;
    private final Queue<Mineral> MineralsPlan;
    private final Queue<ACLMessage> pending = new LinkedList<>();
    private final Queue<ACLMessage> toReject = new LinkedList<>();
    private int load;
    private int lastPlannedLoad;

    public Transporter(MarsModel model) {
        super(Color.GREEN, model);
        capacity = Environment.TRANSPORTER_CAPACITY;
        load = 0;
        lastPlannedLoad = 0;
        MineralsPlan = new LinkedList<>();
    }

    @Override
    public void setup() {
        addBehaviour(new RoutineBehaviour());
        addBehaviour(new AnswerCallBehaviour());
        model.registerOnNoMoreMinerals(this::scheduleRetreat);
    }

    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            removeBehaviourOnDone(this);

            if (Math.abs(Environment.SHIP_POSITION.distance(getPosition())) <= Environment.MINING_DISTANCE)
                load = 0;

            Mineral nextMineral = MineralsPlan.peek();
            Point position = getPosition();

            if (nextMineral != null) {
                Point mineralPosition = nextMineral.getPosition();
                if (Math.abs(position.distance(mineralPosition)) <= Environment.MINING_DISTANCE) {
                    MineralsPlan.poll();
                    int collectible = Math.min(capacity - load, nextMineral.fragments.get());
                    nextMineral.take(collectible);
                    load += collectible;
                }
            }
            moveMovementPlan();
        }

    }

    private class AnswerCallBehaviour extends ContractNetResponder {

        AnswerCallBehaviour() {
            super(Transporter.this, MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET));
        }

        @Override
        public ACLMessage handleCfp(ACLMessage message) {
            String[] content = message.getContent().split(",");
            Point fragmentsPosition = new Point(Integer.parseInt(content[0]), Integer.parseInt(content[1]));
            int totalMinerals = Integer.parseInt(content[2]);

            int cost = getPlanCost();
            int available = capacity - lastPlannedLoad;
            int collectible = Math.min(available, totalMinerals);
            cost += getCost(fragmentsPosition);

            ACLMessage response = message.createReply();
            response.setPerformative(ACLMessage.PROPOSE);
            response.setContent(collectible + "-" + cost);

            pending.add(response);
            return response;
        }

        @Override
        public ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            if (toReject.contains(propose)) {
                ACLMessage response = accept.createReply();
                response.setPerformative(ACLMessage.FAILURE);
                toReject.remove(response);
                return response;
            }

            pending.remove(propose);
            toReject.addAll(pending);
            pending.clear();

            String[] coordinates = cfp.getContent().split(",");

            Point fragmentsPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));

            addMovementPlan(fragmentsPosition, 0);

            Mineral mineral = getMineralAt(fragmentsPosition);
            if (mineral != null) {
                MineralsPlan.add(mineral);
                int collectible = Math.min(capacity - lastPlannedLoad, mineral.fragments.get());
                lastPlannedLoad += collectible;
                if (lastPlannedLoad >= capacity) {
                    addMovementPlan(Environment.SHIP_POSITION, 0);
                    lastPlannedLoad = 0;
                }
            }
            ACLMessage response = accept.createReply();
            response.setPerformative(ACLMessage.INFORM);
            return response;
        }

        @Override
        public void handleRejectProposal(ACLMessage cfp, ACLMessage proposal, ACLMessage reject) {
            toReject.remove(proposal);
        }

        private Mineral getMineralAt(Point position) {
            Mineral found = null;
            Set<MarsAgent> agents = model.getAgentsAt(position);
            for (MarsAgent agent : agents) {
                if (agent instanceof Mineral) {
                    found = (Mineral) agent;
                    break;
                }
            }

            return found;
        }
    }

}
