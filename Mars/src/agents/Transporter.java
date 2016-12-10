package agents;

import behaviours.TransporterMoveBehaviour;
import main.Environment;
import main.Simulation;
import main.Transport;
import main.TransportMovement;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;
import main.MarsModel;

/**
 *
 * @author diogo
 */
public class Transporter extends MarsAgent {

//    private int capacity;
//    private int available;
    private final Queue<TransportMovement> transports;
    private TransportMovement currentTransport;
    private int transportsCost;
    private final Point shipPosition;
    
    public Transporter(/*int capacity, */MarsModel model) {
        super(Color.BLUE, model);
//        this.capacity = capacity;
//        available = capacity;
        transports = new LinkedList<>();
        currentTransport = null;
        transportsCost = 0;
        this.shipPosition = model.shipPosition;
    }

    @Override
    protected void setup() {
        int bound = Environment.SIZE + 1;
        for (int i = 0; i < 10; i++) {
            Point place = new Point(Simulation.random.nextInt(bound), Simulation.random.nextInt(bound));
            TransportMovement transport = new TransportMovement(place, 0, shipPosition, shipPosition);
            addTransport(transport);
        }
        Point place = new Point(bound, bound);
        TransportMovement transport = new TransportMovement(place, 0, shipPosition, shipPosition);
        addTransport(transport);

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
