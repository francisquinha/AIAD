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
    private final Queue<MineralFragments> remainingMineralPlan;
    private final int totalExtractTime;
    private AID[] otherTransporters;
    private int extractTime;

    public Producer(MarsModel model) {
        super(Color.GREEN, model);
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
        model.registerOnNoMoreFragments(this::scheduleRetreat);
    }

    @Override
    int getPlanCost() {
        return super.getPlanCost() + totalExtractTime + extractTime;
    }

    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {

            if (getDone()) {
                if (getPosition().distance(Environment.SHIP_POSITION) <= 0)
                    removeBehaviour(this);
                else moveMovementPlan();
            }

            MineralFragments fragments = remainingMineralPlan.poll();
            if (fragments != null) {
                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                int quantity = Math.min(fragments.quantity.get(), fragments.previewQuantity.get());
                if (quantity != 0) {
                    msg.setContent(fragments.getPosition().x + "," + fragments.getPosition().y + "," + fragments.previewQuantity.get());
                    for (AID aid : otherTransporters)
                        msg.addReceiver(aid);
                    addBehaviour(new RequestTransporterBehaviour(fragments, msg));
                }
            }

            Mineral nextMineral = mineralPlan.peek();
            if (nextMineral == null)
                return;

            if (Math.abs(getPosition().distance(nextMineral.getPosition())) <= 1) {
                if (extractTime <= 0)
                    extractTime = nextMineral.getQuantity();
                else {
                    if (extractTime == 1) {
                        Point mineralPosition = nextMineral.getPosition();

                        mineralPlan.poll();
                        fragments = nextMineral.mine();

                        ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                        msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                        msg.setContent(mineralPosition.x + "," + mineralPosition.y + "," + fragments.quantity.get());
                        for (AID aid : otherTransporters)
                            msg.addReceiver(aid);
                        addBehaviour(new RequestTransporterBehaviour(fragments, msg));
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

            cost += getCost(mineralPosition) - 1; // Because it only needs to be adjacent, not in it

            response.setContent("" + cost);
            return response;
        }

        @Override
        public ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept) {
            String[] coordinates = cfp.getContent().split(",");

            Point mineralPosition = new Point(Integer.parseInt(coordinates[0]), Integer.parseInt(coordinates[1]));

            addMovementPlan(mineralPosition, 1);

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

        private final MineralFragments fragments;

        RequestTransporterBehaviour(MineralFragments fragments, ACLMessage msg) {
            super(Producer.this, msg);
            this.fragments = fragments;
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
            fragments.previewTake(load);
            System.out.printf("%s assigned to Mineral Fragments at (%d, %d) - quantity %d - load %d - remaining - %d\n",
                    proposal.getSender().getLocalName(), (int) fragments.getPosition().getX(),
                    (int) fragments.getPosition().getY(), fragments.quantity.get(), load, fragments.previewQuantity.get());
            chosen.add(proposal);

            if (fragments.previewQuantity.get() > 0) {
                remainingMineralPlan.add(fragments);
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
