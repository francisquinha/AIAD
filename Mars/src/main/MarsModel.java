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
import uchicago.src.sim.gui.RectNetworkItem;
import uchicago.src.sim.space.Discrete2DSpace;
import uchicago.src.sim.space.Multi2DGrid;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

/**
 * @author diogo
 */
public class MarsModel extends Repast3Launcher {

    private final Set<Mineral> minerals;
    private final Set<MineralFragments> fragments;
    private final List<Runnable> noMineralsCallbacks;
    private final List<Runnable> noFragmentsCallbacks;
    private ContainerController mainContainer;
    private ArrayList<ArrayList<ConcurrentSkipListSet<MarsAgent>>> agents;
    private Discrete2DSpace space;
    private DisplaySurface displaySurface;
    private List<MarsNode> nodes;

    MarsModel() {
        minerals = new HashSet<>();
        fragments = new HashSet<>();
        noFragmentsCallbacks = new LinkedList<>();
        noMineralsCallbacks = new LinkedList<>();
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> getAgents(Class<T> cl) {
        Set<T> found = new HashSet<>();
        for (MarsNode node : nodes) {
            if (node.agent != null && cl.isAssignableFrom(node.agent.getClass()))
                found.add((T) node.agent);
        }

        return found;
    }

    public Set<MarsAgent> getAgentsAt(Point position) {
        return agents.get(position.x).get(position.y);
    }

    public void moveAgent(MarsAgent agent, Point newPosition) {
        Point position = agent.getPosition();
        Set<MarsAgent> agentsAtPosition = agents.get(position.x).get(position.y);
        if (agentsAtPosition.contains(agent)) {
            agentsAtPosition.remove(agent);
            agents.get(newPosition.x).get(newPosition.y).add(agent);
            agent.node.setX(newPosition.x);
            agent.node.setY(newPosition.y);
        }
    }

    public void addAgent(MarsAgent agent, Point position) {
        agents.get(position.x).get(position.y).add(agent);
        agent.node.setX(position.x);
        agent.node.setY(position.y);
        nodes.add(agent.node);

        if (agent instanceof Mineral)
            minerals.add((Mineral) agent);
        else if (agent instanceof MineralFragments)
            fragments.add((MineralFragments) agent);
    }

    public void removeAgent(MarsAgent agent) {
        Point position = agent.getPosition();
        nodes.remove(agent.node);
        agents.get(position.x).get(position.y).remove(agent);
        agent.doDelete();

        if (agent instanceof Mineral) {
            minerals.remove(agent);
            if (minerals.isEmpty())
                fireNoMoreMinerals();
        } else if (agent instanceof MineralFragments) {
            fragments.remove(agent);
            if (fragments.isEmpty() && minerals.isEmpty())
                fireNoMoreFragments();
        }
    }

    public void registerOnNoMoreFragments(Runnable callback) {
        noFragmentsCallbacks.add(callback);
    }

    private void fireNoMoreMinerals() {
        noMineralsCallbacks.forEach(Runnable::run);
    }

    private void fireNoMoreFragments() {
        noFragmentsCallbacks.forEach(Runnable::run);
    }

    @Override
    public void begin() {
        super.begin();
        buildDisplay();
        spreadMinerals();
        assignSpotterSpaces();
    }

    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        mainContainer = rt.createMainContainer(p1);

        try {
            buildSpace();
            buildAgents();
        } catch (StaleProxyException | FIPAException e) {
            System.out.println("Could not launch the agents! " + e.getMessage());
        }
    }

    private void buildAgents() throws StaleProxyException, FIPAException {
        nodes = new ArrayList<>();
        MarsNode shipNode = new MarsNode(null, new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        shipNode.setColor(Color.WHITE);
        nodes.add(shipNode);
        buildAgents(Environment.MINERALS, MarsAgent.Ontologies.MINERAL, () -> new Mineral(this));
        buildAgents(Environment.SPOTTERS, MarsAgent.Ontologies.SPOTTER, () -> new Spotter(this));
        buildAgents(Environment.PRODUCERS, MarsAgent.Ontologies.PRODUCER, () -> new Producer(this));
        buildAgents(Environment.TRANSPORTERS, MarsAgent.Ontologies.TRANSPORTER,
                () -> new Transporter(this));
    }

    private void spreadMinerals() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        for (Mineral mineral : getAgents(Mineral.class)) {
            Point newPosition = new Point(r.nextInt(0, Environment.SIZE), r.nextInt(0, Environment.SIZE));
            mineral.move(newPosition);
        }
    }

    private <T extends MarsAgent> List<T> buildAgents(int count, String ontology, Supplier<T> supplier) throws FIPAException, StaleProxyException {
        List<T> createdAgents = new LinkedList<>();

        for (int i = 0; i < count; i++) {
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

            addAgent(agent, Environment.SHIP_POSITION);
            mainContainer.acceptNewAgent(nickname, agent).start();
        }

        return createdAgents;
    }

    private void buildSpace() {
        space = new Multi2DGrid(Environment.SIZE, Environment.SIZE, true);
        displaySurface = new DisplaySurface(this, "Mars");
        registerDisplaySurface("Mars", displaySurface);

        agents = new ArrayList<>(Environment.SIZE);
        for (int x = 0; x < Environment.SIZE; x++) {
            ArrayList<ConcurrentSkipListSet<MarsAgent>> column = new ArrayList<>(Environment.SIZE);
            for (int y = 0; y < Environment.SIZE; y++)
                column.add(y, new ConcurrentSkipListSet<>());

            agents.add(x, column);
        }
    }

    private void buildDisplay() {
        Object2DDisplay display = new Network2DGridDisplay(space);
        display.setObjectList(nodes);
        displaySurface.addDisplayableProbeable(display, "Mars");
        addSimEventListener(displaySurface);
        displaySurface.display();
        getSchedule().scheduleActionAtInterval(1, displaySurface, "updateDisplay", Schedule.LAST);
    }

    private void assignSpotterSpaces() {
        int spaceSize = Environment.SIZE;
        Set<Spotter> spotters = getAgents(Spotter.class);
        int spottersCount = spotters.size();
        int height = spaceSize / spottersCount;
        int currentOffset = 0;

        for (Spotter spotter : spotters) {
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
