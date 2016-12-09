package agents;

import behaviours.TransporterMoveBehaviour;
import main.Environment;
import main.Simulation;
import main.Transport;
import main.TransportMovement;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.LinkedList;
import java.util.Queue;


/**
 *
 * @author diogo
 */
public class Transporter extends MarsAgent {

//    private int capacity;
//    private int available;
    private Queue<TransportMovement> transports;
    private TransportMovement currentTransport;
    private int transportsCost;
    private Point2D.Double shipPosition;
    
    public Transporter(/*int capacity, */Point2D.Double shipPosition) {
        super(Color.BLUE);
//        this.capacity = capacity;
//        available = capacity;
        transports = new LinkedList<>();
        currentTransport = null;
        transportsCost = 0;
        this.shipPosition = shipPosition;
    }

    @Override
    protected void setup() {
        for (int i = 0; i < 10; i++) {
            Point2D.Double place = new Point2D.Double(Simulation.random.nextInt(
                    Environment.SIZE + 1), Simulation.random.nextInt(Environment.SIZE + 1));
            TransportMovement transport = new TransportMovement(place, 0, shipPosition, shipPosition);
            System.out.printf("%d - %d\n", transport.getCost(), getTransportCost(transport));
            addTransport(transport);
        }
        this.addBehaviour(new TransporterMoveBehaviour(this));
    }

    public int getTransportCost(Transport transport) {
        TransportMovement transportMovement = new TransportMovement(transport.getPlace(), transport.getQuantity(), shipPosition, shipPosition);
        if (currentTransport == null)
            return transportsCost + transports.size() + transportMovement.getOneWayCost();
        return currentTransport.getCost() + transportsCost + transports.size() + 1 + transportMovement.getOneWayCost();
    }

    public void addTransport(Transport transport) {
        TransportMovement transportMovement = new TransportMovement(transport.getPlace(), transport.getQuantity(), shipPosition, shipPosition);
        transports.add(transportMovement);
        transportsCost += transportMovement.getCost();
    }

    public void getNextTransport() {
        currentTransport = transports.poll();
        if (currentTransport != null)
            transportsCost -= currentTransport.getCost();
    }

    public TransportMovement getCurrentTransport() {
        return currentTransport;
    }

}
