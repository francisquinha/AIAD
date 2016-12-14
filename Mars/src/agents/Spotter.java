package agents;

import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import main.Environment;
import main.MarsModel;
import sajas.core.behaviours.CyclicBehaviour;
import sajas.proto.ContractNetInitiator;
import sajas.proto.ProposeInitiator;
import sajas.proto.ProposeResponder;

import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * @author diogo
 */
public class Spotter extends MovingAgent {

    private final List<Mineral> mineralsFound;
    private final HashMap<String, AID> areaOwners = new HashMap<>();
    private final HashMap<String, AID> areaNegotiations = new HashMap<>();
    private AID[] otherSpotters;
    private AID[] otherProducers;
    private String localName;
    private int rowYOffset;
    private int rowHeight;

    public Spotter(MarsModel model) {
        super(Color.RED, model);
        mineralsFound = new LinkedList<>();
    }

    @Override
    protected void setup() {
        localName = getLocalName();
        AID[] allSpotters = getAgents(MarsAgent.Ontologies.SPOTTER);
        otherSpotters = Arrays.stream(allSpotters)
                .filter((aid) -> !aid.getLocalName().equals(localName))
                .toArray(AID[]::new);
        otherProducers = getAgents(MarsAgent.Ontologies.PRODUCER);

        addBehaviour(new AnswerAreaRequestBehaviour());
        addBehaviour(new AcknowledgeAreaBehaviour());
        addBehaviour(new ScanBehaviour());

    }

    public void assignRow(int yOffset, int height) {
        addBehaviour(new RequestAreaBehaviour(yOffset, height));
    }

    private class RequestAreaBehaviour extends ProposeInitiator {

        private final int yOffset;
        private final int height;
        private final Set<String> awaitingConfirmation;

        RequestAreaBehaviour(int yOffset, int height) {
            super(Spotter.this, null);
            this.yOffset = yOffset;
            this.height = height;
            awaitingConfirmation = new HashSet<>();
        }

        @Override
        @SuppressWarnings("UseOfObsoleteCollectionType")
        protected Vector<ACLMessage> prepareInitiations(ACLMessage initialPropose) {
            Vector<ACLMessage> messages = new Vector<>();
            ACLMessage proposeMessage = buildMessage();
            messages.add(proposeMessage);
            for (AID spotter : otherSpotters)
                awaitingConfirmation.add(spotter.getLocalName());
            acceptAssignment();
            return messages;
        }

        @Override
        protected void handleAcceptProposal(ACLMessage response) {
            AID sender = response.getSender();
            awaitingConfirmation.remove(sender.getLocalName());
            acceptAssignment();
        }

        void acceptAssignment() {
            if (awaitingConfirmation.isEmpty()) {
                System.out.println(localName + " assigned to rows " + yOffset + "-" + height);
                rowYOffset = yOffset;
                rowHeight = height;
                sendAreaConfirmation();
                addBehaviour(new MoveBehaviour());
            }
        }

        @Override
        protected void handleRejectProposal(ACLMessage response) {
            System.out.println(localName + " was rejected for rows " + yOffset + "-" + height);
        }

        ACLMessage buildMessage() {
            ACLMessage message = new ACLMessage(ACLMessage.PROPOSE);
            message.setContent(yOffset + "-" + height);
            message.setProtocol(FIPANames.InteractionProtocol.FIPA_PROPOSE);

            for (AID spotter : otherSpotters)
                message.addReceiver(spotter);

            return message;
        }

        private void sendAreaConfirmation() {
            ACLMessage message = new ACLMessage(ACLMessage.INFORM);
            message.setContent(yOffset + "-" + height);
            message.setSender(getAID());
            for (AID receiver : otherSpotters)
                message.addReceiver(receiver);

            send(message);
        }
    }

    private class AnswerAreaRequestBehaviour extends ProposeResponder {

        AnswerAreaRequestBehaviour() {
            super(Spotter.this, ProposeResponder.createMessageTemplate(FIPANames.InteractionProtocol.FIPA_PROPOSE));
        }

        @Override
        protected ACLMessage prepareResponse(ACLMessage propose) {
            String area = propose.getContent();
            ACLMessage message = propose.createReply();

            if (areaOwners.containsKey(area))
                message.setPerformative(ACLMessage.REJECT_PROPOSAL);
            else if (areaNegotiations.containsKey(area))
                message.setPerformative(ACLMessage.REJECT_PROPOSAL);
            else {
                areaNegotiations.put(area, propose.getSender());
                message.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
            }

            return message;
        }
    }

    private class AcknowledgeAreaBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.INFORM));
            if (msg == null)
                return;

            AID sender = msg.getSender();
            String area = msg.getContent();
            if (areaNegotiations.containsKey(area)) {
                AID registered = areaNegotiations.get(area);
                if (registered.getLocalName().equals(sender.getLocalName())) {
                    areaNegotiations.remove(area);
                    areaOwners.put(area, sender);
                    reset();
                }
            }
        }
    }

    private class MoveBehaviour extends CyclicBehaviour {

        final Point finalPosition;

        MoveBehaviour() {
            if (rowYOffset != node.getY())
                addMovementPlan(new Point(0, rowYOffset), 0);

            int maxX = Environment.SIZE - 1;

            finalPosition = new Point(rowHeight % 2 == 0 ? 0 : maxX, rowYOffset + rowHeight);
            if (finalPosition.y >= Environment.SIZE) finalPosition.y = Environment.SIZE - 1;

            boolean direction = true;
            Point target = new Point(0, rowYOffset);
            do {
                if (direction) {
                    target.x = maxX;
                    direction = false;
                } else {
                    target.x = 0;
                    direction = true;
                }
                if (target.y < finalPosition.getY())
                    target.y++;
                addMovementPlan(target, 0);
            }
            while (target.distance(finalPosition) > 0);
        }

        @Override
        public void action() {
            if (getDone()) {
                if (getPosition().distance(Environment.SHIP_POSITION) <= 0)
                    removeBehaviour(this);
            }
            moveMovementPlan();
            if (getPosition().distance(finalPosition) == 0)
                scheduleRetreat();
        }
    }

    public class ScanBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if (getDone()) {
                if (getPosition().distance(Environment.SHIP_POSITION) <= 0)
                    removeBehaviour(this);
            }
            Point newPosition = getPosition();
            Set<MarsAgent> agents = model.getAgentsAt(newPosition);
            for (MarsAgent agent : agents) {
                if (agent instanceof Mineral) {
                    Mineral mineral = (Mineral) agent;
                    mineral.node.setColor(Color.YELLOW);
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    msg.setOntology(MarsAgent.Ontologies.SPOTTER);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    msg.setContent(newPosition.x + "," + newPosition.y);
                    for (AID producer : otherProducers)
                        msg.addReceiver(producer);

                    mineralsFound.add(mineral);
                    if (mineral.getExtractable())
                        addBehaviour(new RequestProducerBehaviour(msg, mineral));
                    else
                        mineral.node.setColor(Color.GRAY);
                }
            }
        }

    }

    public class RequestProducerBehaviour extends ContractNetInitiator {

        private final Mineral mineral;

        RequestProducerBehaviour(ACLMessage msg, Mineral mineral) {
            super(Spotter.this, msg);
            this.mineral = mineral;
        }

        @Override
        @SuppressWarnings("unchecked")
        public void handleAllResponses(Vector proposes, Vector responses) {
            int minCost = Integer.MAX_VALUE;
            ACLMessage minCostProposal = null;

            for (Object proposeObj : proposes) {
                ACLMessage propose = (ACLMessage) proposeObj;
                int cost = Integer.parseInt(propose.getContent());
                if (cost < minCost) {
                    minCost = cost;
                    if (minCostProposal != null) {
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

            if (minCostProposal != null) {
                ACLMessage selectedMessage = minCostProposal.createReply();
                selectedMessage.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
                System.out.printf("%s assigned to Mineral at (%d, %d) - quantity %d\n", minCostProposal.getSender().getLocalName(),
                        (int) mineral.getPosition().getX(), (int) mineral.getPosition().getY(), mineral.getQuantity());
                responses.add(selectedMessage);
            }
        }

    }
}
