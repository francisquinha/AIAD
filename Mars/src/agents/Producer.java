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
    private AID[] otherTransporters;

    public Producer(MarsModel model) {
        super(Color.GREEN, model);
        this.mineralPlan = new LinkedList<>();
    }

    @Override
    public void setup() {
        this.otherTransporters = this.getAgents(MarsAgent.Ontologies.TRANSPORTER);
        this.addBehaviour(new RoutineBehaviour());
        this.addBehaviour(new AnswerCallBehaviour());
        this.model.registerOnNoMoreMinerals(this::scheduleRetreat);
    }

    private class RoutineBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if (getDone()) {
                if (Producer.this.getPosition().distance(Environment.SHIP_POSITION) <= 0)
                    removeBehaviour(this);
            }

            Mineral nextMineral = Producer.this.mineralPlan.peek();
            if (nextMineral == null)
                return;

            Point position = Producer.this.getPosition();
            Point mineralPosition = nextMineral.getPosition();
            if (Math.abs(position.distance(mineralPosition)) <= 1) {
                Producer.this.mineralPlan.poll();
                MineralFragments fragments = nextMineral.mine();

                ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                msg.setContent(mineralPosition.x + "," + mineralPosition.y + "," + fragments.quantity.get());
                for (AID aid : otherTransporters)
                    msg.addReceiver(aid);

                Producer.this.addBehaviour(new RequestTransporterBehaviour(fragments, msg));
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

            Mineral mineral = this.getMineralAt(mineralPosition);
            Producer.this.mineralPlan.add(mineral);

            ACLMessage response = accept.createReply();
            response.setPerformative(ACLMessage.INFORM);
            return response;
        }

        private Mineral getMineralAt(Point position) {
            Mineral found = null;
            Set<MarsAgent> agents = Producer.this.model.getAgentsAt(position);
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

            for (Object proposeObj : proposes) {
                ACLMessage propose = (ACLMessage) proposeObj;
                String[] contents = propose.getContent().split("-");
                loads.put(Integer.parseInt(contents[0]), propose);
            }

            Integer[] orderedLoads = loads.keySet().toArray(new Integer[0]);
            Arrays.sort(orderedLoads);

            Queue<ACLMessage> chosen = new LinkedList<>();
            for (Integer load : orderedLoads) {
                if (load >= fragmentsRemaining) {
                    fragmentsRemaining = 0;
                    ACLMessage sender = loads.get(load);
                    chosen.add(sender);
                }
            }

            for (int i = orderedLoads.length - 1; i >= 0; i--) {
                if (fragmentsRemaining == 0)
                    break;

                Integer load = orderedLoads[i];
                fragmentsRemaining -= load;
                ACLMessage proposal = loads.get(load);
                chosen.add(proposal);
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

            if (chosen.isEmpty())
                this.reset();
        }

        @Override
        public void handleAllResultNotifications(Vector v) {
            for (Object obj : v) {
                ACLMessage message = (ACLMessage) obj;
                if (message.getPerformative() == ACLMessage.FAILURE) {
                    this.reset();
                    return;
                }
            }

            if (fragmentsRemaining > 0) {
                String[] content = initialMessage.getContent().split(",");
                content[2] = fragmentsRemaining + "";
                initialMessage.setContent(content[0] + "," + content[1] + "," + content[2]);
                this.reset(initialMessage);
            }
        }

    }
}
