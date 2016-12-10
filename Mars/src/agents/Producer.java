package agents;

import jade.core.AID;
import main.Environment;
import main.Movement;
import main.Simulation;
import main.TransportMovement;
import uchicago.src.sim.space.Discrete2DSpace;

import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import behaviours.ProducerMoveBehaviour;
import behaviours.TransporterMoveBehaviour;

/**
 *
 * @author diogo
 */
public class Producer extends MarsAgent {
    
	// Movement
	private Queue<Movement> producersMoves;
	private Movement currentMovement, lastMove;
	private int movementCost;
	private Point shipFinalPosition;
	// Minerio
	private Queue<Mineral> minerioFounded;
	private Mineral currentMineral;
	// Transporters
	private ArrayList<AID> transporters;
	private int bestCost;
	private AID bestTransporter;
	
    public Producer(Point shipPosition, Discrete2DSpace space) {
        super(Color.GREEN, space);
        // Movement
        this.producersMoves = new LinkedList<Movement>();
        this.currentMovement = null;
        this.movementCost = 0;
        this.shipFinalPosition = shipPosition;
        // Minerio
        this.minerioFounded = new LinkedList<Mineral>();
        this.currentMineral = null;
        resetTransportersValues();
    }

	private void resetTransportersValues() {
        // Transporters
        this.transporters = new ArrayList<AID>();
        this.bestCost = 0;
        this.bestTransporter = null;
	}
    
    @Override
    protected void setup() {
        int bound = Environment.SIZE + 1;
        for (int i = 0; i < 10; i++) {
            Point place = new Point(Simulation.random.nextInt(bound), Simulation.random.nextInt(bound));
            System.out.printf("Producer - %d\n", getMovementCost(place));
            addMovement(place);
        }
        /*Point place = new Point(bound, bound);
        System.out.printf("Producer - %d\n", getMovementCost(place));
        addMovement(place);
*/
        this.addBehaviour(new ProducerMoveBehaviour(this));
    }
    
    //Producer Movement
    public int getMovementCost(Point place) {
    	Movement movement2Place = new Movement(shipFinalPosition, place);
    	if(currentMovement == null)
    		return movementCost + producersMoves.size() + 1 + movement2Place.getSteps();
        return currentMovement.getSteps() + movementCost + producersMoves.size() + 1 + movement2Place.getSteps();
    }
    
    public void addMovement(Point place) {
    	Movement movement2Place = new Movement(shipFinalPosition, place);
    	producersMoves.add(movement2Place);
    	movementCost += movement2Place.getSteps();
    }
    
    public void getNextMovement() {    	
    	if(!producersMoves.isEmpty()){
    		currentMovement = producersMoves.poll();
    		movementCost -= currentMovement.getSteps();
    	}    	
    }
    
    public Movement getCurrentMovement(){
    	return currentMovement;
    }
    
    //Minerio
    public void foundMinerio(Mineral minerio){
    	minerioFounded.add(minerio);
    	if(currentMineral == null)
    		nextMinerio();
    	
    }
    
    private void nextMinerio() {
    	currentMineral = minerioFounded.poll();
    	if(currentMineral != null)
    		sendRequestToTransporters();
	}

    // Transporter Handler
	public void sendRequestToTransporters(){
    	/* Complete with message sending */
    }
    
    public void transporterAnswer(AID aid, int cost){
    	if(bestCost < cost){
    		bestCost = cost;
    		bestTransporter = aid;
    	}    	
    	
    	transporters.remove(aid);
    	if(transporters.size() <= 0)
    		attributeMinerioToTransporter();    	
    }
    
    public void attributeMinerioToTransporter(){
    	/*
    	 *  Necessário enviar mensagem para o Transporter identificado no bestTransporter
    	 */
    	
    	//Reset Values to attribute minerio and get next minerio if exists
    	resetTransportersValues();
    	nextMinerio();
    }    
}
