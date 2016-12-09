package main;

import agents.MarsAgent;
import agents.Producer;
import agents.Spotter;
import agents.Transporter;
import jade.core.AID;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.wrapper.StaleProxyException;
import sajas.core.Runtime;
import sajas.domain.AMSService;
import sajas.domain.DFService;
import sajas.sim.repast3.Repast3Launcher;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DGridDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.network.DefaultDrawableNode;
import uchicago.src.sim.space.Diffuse2D;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Multi2DGrid;
import uchicago.src.sim.space.Object2DGrid;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

/**
 *
 * @author diogo
 */
public class MarsModel extends Repast3Launcher {
    
    private ContainerController mainContainer;
    private Schedule schedule;
    private Discrete2DSpace space;
    private DisplaySurface displaySurface;
    private Object2DDisplay display;
    private List<DefaultDrawableNode> nodes;
    
    private List<Producer> producers;
    private List<Spotter> spotters;
    private List<Transporter> transporters;

    private Point2D.Double shipPosition;
    
    @Override
    public void begin() {
        super.begin();
        this.buildSpace();
        this.buildDisplay();
        this.assignSpotterSpaces();
    }
    
    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        this.mainContainer = rt.createMainContainer(p1);

        try {
            buildAgents();
        } catch(StaleProxyException | FIPAException e) {
            System.out.println("Could not launch the agents! " + e.getMessage());
        }
    }
    
    protected void buildAgents() throws StaleProxyException, FIPAException {
        this.nodes = new ArrayList<>();
        this.spotters = buildAgents(Environment.SPOTTERS, MarsAgent.Ontologies.SPOTTER, () -> new Spotter());
        this.producers = buildAgents(Environment.PRODUCERS, MarsAgent.Ontologies.PRODUCER, () -> new Producer());
        this.transporters = buildAgents(Environment.TRANSPORTERS, MarsAgent.Ontologies.TRANSPORTER, () -> new Transporter(shipPosition));
    }
    
    protected <T extends MarsAgent> List<T> buildAgents(int count, String ontology, Supplier<T> supplier) throws FIPAException, StaleProxyException {
        List<T> createdAgents = new LinkedList<>();
        
        for(int i = 0; i < count; i++) {
            T agent = supplier.get();
            String nickname = ontology + i;
            AID aid = new AID();
            aid.setName(nickname);
            agent.setAID(aid);
            createdAgents.add(agent);
            
            DFAgentDescription df = new DFAgentDescription();
            df.setName(aid);
            df.addOntologies(ontology);
            AMSService.register(agent);
            DFService.register(agent, df);
            
            this.nodes.add(agent.node);
            this.mainContainer.acceptNewAgent(nickname, agent).start();
        }
        
        return createdAgents;
    }
    
    protected void buildSpace() {
        this.space = new Diffuse2D(Environment.SIZE * Environment.CELL_SIZE, Environment.SIZE);
        this.displaySurface = new DisplaySurface(this, "Mars");
        this.registerDisplaySurface("Mars", this.displaySurface);
    }
    
    protected void buildDisplay() {
        this.display = new Network2DGridDisplay(this.space);
        this.display.setObjectList(this.nodes);
        this.displaySurface.addDisplayableProbeable(this.display, "Mars");
        addSimEventListener(this.displaySurface);
        this.displaySurface.display();
        getSchedule().scheduleActionAtInterval(1, this.displaySurface, "updateDisplay", Schedule.LAST);
    }
    
    protected void assignSpotterSpaces() {
        int spaceSize = Environment.SIZE;
        int spottersCount = this.spotters.size();
        int height = spaceSize/spottersCount;
        int currentOffset = 0;
        
        for(Spotter spotter : this.spotters) {
            spotter.assignRow(currentOffset, height);
            currentOffset += height;
        }
        
    }

    protected Point2D.Double getShipPosition() {
        return this.shipPosition;
    }
  
    @Override
    public String[] getInitParam() {
        return new String[0];
    }

    @Override
    public String getName() {
        return "Mars simple model";
    }
}