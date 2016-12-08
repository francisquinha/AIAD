package agents;

import behaviours.TransporterMoveBehaviour;
import main.Movement;
import main.Simulation;
import main.Transport;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.PriorityQueue;
import java.util.Queue;


/**
 *
 * @author diogo
 */
public class Transporter extends MarsAgent {

    private int capacity;
    private int available;
    private Queue<TransportMovement> transports;
    private int cost;
    private Point2D.Double shipPosition;
    
    public Transporter(int capacity, Point2D.Double shipPosition) {
        super(Color.BLUE);
        this.capacity = capacity;
        available = capacity;
        transports = new PriorityQueue<>();
        cost = 0;
        this.shipPosition = shipPosition;
    }

    public int getTransportTotalCost(Transport transport) {
        TransportMovement transportMovement = new TransportMovement(transport, shipPosition, shipPosition);
        return cost + transportMovement.getCost();
    }

    public void addTransport(Transport transport) {
        TransportMovement transportMovement = new TransportMovement(transport, shipPosition, shipPosition);
        cost += transportMovement.getCost();
        transports.add(transportMovement);
    }

    public Point2D.Double getPosition() {
        return new Point2D.Double(this.node.getX(), this.node.getY());
    }

    @Override
    protected void setup() {
        this.node.setX(Simulation.random.nextInt(101));
        this.node.setY(Simulation.random.nextInt(101));
        this.addBehaviour(
                new TransporterMoveBehaviour(this,
                        new Point2D.Double(Simulation.random.nextInt(101), Simulation.random.nextInt(101))));
    }

    private class TransportMovement extends Transport {

        private Movement movement2Place;
        private Movement movement2Ship;

        private TransportMovement(Transport transport, Point2D.Double transporterPosition, Point2D.Double shipPosition) {
            super(transport.getPlace(), transport.getQuantity());
            movement2Place = new Movement(transporterPosition, this.getPlace());
            movement2Ship = new Movement(this.getPlace(), shipPosition);
        }

        private int getCost() {
            return movement2Place.getSteps() + movement2Ship.getSteps();
        }

    }

}
