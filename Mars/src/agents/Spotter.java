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

            for(AID spotter : otherSpotters)
                awaitingConfirmation.add(spotter.getLocalName());

            return messages;
        }

        @Override
        protected void handleAcceptProposal(ACLMessage response) {
            if(awaitingConfirmation.isEmpty())
                return;

            AID sender = response.getSender();
            awaitingConfirmation.remove(sender.getLocalName());

            if(awaitingConfirmation.isEmpty()) {
                System.out.println(localName + " has now " + yOffset + "-" + height);
                rowYOffset = yOffset;
                rowHeight = height;
                sendAreaConfirmation();
                addBehaviour(new MoveBehaviour());
            }
        }

        @Override
        protected void handleRejectProposal(ACLMessage response) {
            System.out.println(localName + " was rejected for " + yOffset + "-" + height);
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
            for(AID receiver : otherSpotters)
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

            if(areaOwners.containsKey(area))
                message.setPerformative(ACLMessage.REJECT_PROPOSAL);
            else if(areaNegotiations.containsKey(area))
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
            if(msg == null)
                return;

            AID sender = msg.getSender();
            String area = msg.getContent();
            if(areaNegotiations.containsKey(area)) {
                AID registered = areaNegotiations.get(area);
                if(registered.getLocalName().equals(sender.getLocalName())) {
                    areaNegotiations.remove(area);
                    areaOwners.put(area, sender);
                    reset();
                }
            }
        }
    }

    private class MoveBehaviour extends CyclicBehaviour {

        private final Queue<Point> movementPlan;

        MoveBehaviour() {

            Point position;
            if (rowYOffset != node.getY()) {
                position = new Point(0, rowYOffset);
                Point from = new Point((int) node.getX(), (int) node.getY());
                movementPlan = getPlanToPosition(from, position,0);

            }
            else {
                position = new Point((int) node.getX(), rowYOffset);
                movementPlan = new LinkedList<>();
            }

            int maxX = Environment.SIZE - 1;

            int targetX = rowHeight % 2 == 0 ? 0 : maxX;
            int targetY = rowYOffset + rowHeight - 1;

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

                movementPlan.offer(nextMove);
                position.x += nextMove.x;
                position.y += nextMove.y;
            }
            movementPlan.addAll(getPlanToPosition(position, Environment.SHIP_POSITION, 0));
        }

        @Override
        public void action() {
            Point nextMove = movementPlan.poll();
            if(nextMove == null) {
                done = true;
                removeBehaviour(this);
            }
            else
                translate(nextMove);
        }
    }

    public class ScanBehaviour extends CyclicBehaviour {

        @Override
        public void action() {
            if(done)
                removeBehaviour(this);

            Point newPosition = getPosition();
            Set<MarsAgent> agents = model.getAgentsAt(newPosition);
            for(MarsAgent agent : agents) {
                if(agent instanceof Mineral) {
                    Mineral mineral = (Mineral)agent;
                    mineral.node.setColor(Color.YELLOW);
                    ACLMessage msg = new ACLMessage(ACLMessage.CFP);
                    msg.setOntology(MarsAgent.Ontologies.SPOTTER);
                    msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
                    msg.setContent(newPosition.x + "," + newPosition.y);
                    for(AID producer : otherProducers)
                        msg.addReceiver(producer);

                    mineralsFound.add(mineral);
                    addBehaviour(new RequestProducerBehaviour(msg));
                }
            }
        }

    }

    public class RequestProducerBehaviour extends ContractNetInitiator {

        RequestProducerBehaviour(ACLMessage msg) {
            super(Spotter.this, msg);
        }

        @Override
        @SuppressWarnings("unchecked")
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
            System.out.println("Got a producer for a mineral");
        }

    }
}
