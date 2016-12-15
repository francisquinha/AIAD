package agents;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.Environment;
import main.MarsModel;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.proto.ContractNetInitiator;
import sajas.proto.ContractNetResponder;

import java.awt.*;
import java.util.*;
import java.util.Queue;

/**
 * @author diogo
 */
public class Producer extends MovingAgent {

    private final Queue<Mineral> mineralPlan;
    private final Queue<Mineral> remainingMineralPlan;
    private final int totalExtractTime;
    private AID[] otherTransporters;
    private int extractTime;

    public Producer(MarsModel model) {
        super(Color.BLUE, model);
        mineralPlan = new LinkedList<>();
        remainingMineralPlan = new LinkedList<>();
        extractTime = 0;
        totalExtractTime = 0;
    }

    @Override
    public void setup() {
        otherTransporters = getAgents(MarsAgent.Ontologies.TRANSPORTER);
        addBehaviour(new RoutineBehaviour());
        addBehaviour(new AnswerCallBehaviour());
        model.registerOnNoMoreMinerals(this::scheduleRetreat);
    }

    @Override
    int getPlanCost() {
        return super.getPlanCost() + totalExtractTime + extractTime;
    }

    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {

            if (getDone()) {
                if (getPosition().distance(Environment.SHIP_POSITION) <= 0) {
                    removeBehaviour(this);
                    model.removeAgent(Producer.this);
                }
                else moveMovementPlan();
            }

            Mineral remainingMineral = remainingMineralPlan.poll();
            if (remainingMineral != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                int quantity = Math.min(remainingMineral.fragments.get(), remainingMineral.previewFragments.get());
                if (quantity != 0) {
                    msg.setContent(remainingMineral.getPosition().x + "," + remainingMineral.getPosition().y + "," + remainingMineral.previewFragments.get());
                    for (AID aid : otherTransporters)
                        msg.addReceiver(aid);
                    addBehaviour(new RequestTransporterBehaviour(remainingMineral, msg));
                }
            }

            Mineral nextMineral = mineralPlan.peek();
            if (nextMineral == null)
                return;

            if (Math.abs(getPosition().distance(nextMineral.getPosition())) <= Environment.MINING_DISTANCE) {
                if (extractTime <= 0)
                    extractTime = nextMineral.getQuantity();
                else {
                    if (extractTime == 1) {
                        Point mineralPosition = nextMineral.getPosition();

                        mineralPlan.poll();
                        nextMineral.node.setColor(Color.MAGENTA);

                        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                        msg.setContent(mineralPosition.x + "," + mineralPosition.y + "," + nextMineral.fragments.get());
                        for (AID aid : otherTransporters)
                            msg.addReceiver(aid);
                        addBehaviour(new RequestTransporterBehaviour(nextMineral, msg));
                    }
                    extractTime--;
                }
            } else {
                moveMovementPlan();
            }
        }

    }

    private class AnswerCallBehaviour extends ContractNetResponder {

        AnswerCallBehaviour() {
            super(Producer.this, MessageTemplate.and(
                    MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
                    MessageTemplate.MatchOntology(MarsAgent.Ontologies.SPOTTER)));
        }

        @Override
        public ACLMessage handleCfp(ACLMessage message) {
            ACLMessage response = message.createReply();
            response.setPerformative(ACLMessage.PROPOSE);

            int cost = getPlanCost();
            String[] coordinates = message.getContent().split(",");
            Point mineralPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));

            cost += getCost(mineralPosition) - Environment.MINING_DISTANCE; // Because it only needs to be adjacent, not in it

            response.setContent("" + cost);
            return response;
        }

        @Override
        public ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            String[] coordinates = cfp.getContent().split(",");

            Point mineralPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));

            addMovementPlan(mineralPosition, Environment.MINING_DISTANCE);

            Mineral mineral = getMineralAt(mineralPosition);
            mineralPlan.add(mineral);

            ACLMessage response = accept.createReply();
            response.setPerformative(ACLMessage.INFORM);
            return response;
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

    private class RequestTransporterBehaviour extends ContractNetInitiator {

        private final Mineral mineral;

        RequestTransporterBehaviour(Mineral mineral, ACLMessage msg) {
            super(Producer.this, msg);
            this.mineral = mineral;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleAllResponses(Vector proposes, Vector responses) {
            Hashtable<Integer, ACLMessage> costs = new Hashtable<>();

            for (Object proposeObj : proposes) {
                ACLMessage propose = (ACLMessage) proposeObj;
                String[] contents = propose.getContent().split("-");
                costs.put(Integer.parseInt(contents[1]), propose);
            }

            Integer[] orderedCosts = costs.keySet().toArray(new Integer[0]);
            Arrays.sort(orderedCosts);

            Queue<ACLMessage> chosen = new LinkedList<>();

            ACLMessage proposal = costs.get(orderedCosts[0]);
            int load = Integer.parseInt(proposal.getContent().split("-")[0]);
            mineral.previewTake(load);
            System.out.printf("%s assigned to Mineral Fragments at (%d, %d) - fragments %d - load %d - remaining - %d\n",
                    proposal.getSender().getLocalName(), (int) mineral.getPosition().getX(),
                    (int) mineral.getPosition().getY(), mineral.fragments.get(), load, mineral.previewFragments.get());
            chosen.add(proposal);

            if (mineral.previewFragments.get() > 0) {
                remainingMineralPlan.add(mineral);
            }

            for (ACLMessage m : chosen) {
                ACLMessage response = m.createReply();
                response.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                responses.add(response);
            }

            proposes.removeAll(chosen);
            for (Object obj : proposes) {
                ACLMessage propose = (ACLMessage) obj;
                ACLMessage response = propose.createReply();
                response.setPerformative(ACLMessage.REJECT_PROPOSAL);
                responses.add(response);
            }
        }

    }
}
