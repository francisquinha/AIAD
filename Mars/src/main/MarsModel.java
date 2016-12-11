package main;

import agents.*;
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
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Multi2DGrid;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 *
 * @author diogo
 */
public class MarsModel extends Repast3Launcher {
    
    private ContainerController mainContainer;
    private ArrayList<ArrayList<ConcurrentSkipListSet<MarsAgent>>> agents;
    private Schedule schedule;
    private Discrete2DSpace space;
    private DisplaySurface displaySurface;
    private Object2DDisplay display;
    private List<MarsNode> nodes;
    
    private final Set<Mineral> minerals;
    private final Set<MineralFragments> fragments;
    private final List<Runnable> noMineralsCallbacks;
    private final List<Runnable> noFragmentsCallbacks;
    
    public MarsModel() {
        this.minerals = new HashSet<>();
        this.fragments = new HashSet<>();
        this.noFragmentsCallbacks = new LinkedList<>();
        this.noMineralsCallbacks = new LinkedList<>();
    }
    
    public <T> Set<T> getAgents(Class<T> cl) {
        Set<T> found = new HashSet<>();
        for(MarsNode node : nodes) {
            if(cl.isAssignableFrom(node.agent.getClass()))
                found.add((T)node.agent);
        }
        
        return found;
    }
    
    public Set<MarsAgent> getAgentsAt(Point position) {
        return this.agents.get(position.x).get(position.y);
    }
    
    public void moveAgent(MarsAgent agent, Point newPosition) {
        Point position = agent.getPosition();
        Set<MarsAgent> agentsAtPosition = this.agents.get(position.x).get(position.y);
        if(agentsAtPosition.contains(agent)) {
            agentsAtPosition.remove(agent);
            this.agents.get(newPosition.x).get(newPosition.y).add(agent);
            agent.node.setX(newPosition.x);
            agent.node.setY(newPosition.y);
        }
    }
    
    public void addAgent(MarsAgent agent, Point position) {
        this.agents.get(position.x).get(position.y).add(agent);
        agent.node.setX(position.x);
        agent.node.setY(position.y);
        this.nodes.add(agent.node);
        
        if(agent instanceof Mineral)
            this.minerals.add((Mineral)agent);
        else if(agent instanceof MineralFragments)
            this.fragments.add((MineralFragments)agent);
    }
    
    public void removeAgent(MarsAgent agent) {
        Point position = agent.getPosition();
        this.nodes.remove(agent.node);
        this.agents.get(position.x).get(position.y).remove(agent);
        agent.doDelete();
        
        if(agent instanceof Mineral) {
            this.minerals.remove(agent);
            if(this.minerals.isEmpty())
                this.fireNoMoreMinerals();
        } else if(agent instanceof MineralFragments) {
            this.fragments.remove(agent);
            if(this.fragments.isEmpty())
                this.fireNoMoreFragments();
        }
    }
    
    public void registerOnNoMoreMinerals(Runnable callback) {
        this.noMineralsCallbacks.add(callback);
    }
    
    public void registerOnNoMoreFragments(Runnable callback) {
        this.noFragmentsCallbacks.add(callback);
    }
    
    public void fireNoMoreMinerals() {
        this.noMineralsCallbacks.forEach(c -> c.run());
    }
    
    public void fireNoMoreFragments() {
        this.noFragmentsCallbacks.forEach(c -> c.run());
    }
    
    @Override
    public void begin() {
        super.begin();
        this.buildDisplay();
        this.spreadMinerals();
        this.assignSpotterSpaces();
    }
    
    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        this.mainContainer = rt.createMainContainer(p1);

        try {
            this.buildSpace();
            this.buildAgents();
        } catch(StaleProxyException | FIPAException e) {
            System.out.println("Could not launch the agents! " + e.getMessage());
        }
    }
    
    protected void buildAgents() throws StaleProxyException, FIPAException {
        this.nodes = new ArrayList<>();
        buildAgents(Environment.SPOTTERS, MarsAgent.Ontologies.SPOTTER, () -> new Spotter(this));
        buildAgents(Environment.PRODUCERS, MarsAgent.Ontologies.PRODUCER, () -> new Producer(this));
        buildAgents(Environment.MINERALS, MarsAgent.Ontologies.MINERAL, () -> new Mineral(this));
        buildAgents(Environment.TRANSPORTERS, MarsAgent.Ontologies.TRANSPORTER, 
                () -> new Transporter(this, Environment.TRANSPORTER_CAPACITY));
    }
    
    protected void spreadMinerals() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for(Mineral mineral : this.getAgents(Mineral.class)) {
            Point newPosition = new Point(r.nextInt(0, Environment.SIZE), r.nextInt(0, Environment.SIZE));
            mineral.move(newPosition);
        }
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
            
            this.addAgent(agent, Environment.SHIP_POSITION);
            this.mainContainer.acceptNewAgent(nickname, agent).start();
        }
        
        return createdAgents;
    }
    
    protected void buildSpace() {
        this.space = new Multi2DGrid(Environment.SIZE, Environment.SIZE, true);
        this.displaySurface = new DisplaySurface(this, "Mars");
        this.registerDisplaySurface("Mars", this.displaySurface);
        
        this.agents = new ArrayList<>(Environment.SIZE);
        for(int x = 0; x < Environment.SIZE; x++) {
            ArrayList<ConcurrentSkipListSet<MarsAgent>> column = new ArrayList<>(Environment.SIZE);
            for(int y = 0; y <Environment.SIZE; y++)
                column.add(y, new ConcurrentSkipListSet<>());
            
            this.agents.add(x, column);
        }
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
        Set<Spotter> spotters = this.getAgents(Spotter.class);
        int spottersCount = spotters.size();
        int height = spaceSize/spottersCount;
        int currentOffset = 0;
        
        for(Spotter spotter : spotters) {
            spotter.assignRow(currentOffset, height);
            currentOffset += height;
        }
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
