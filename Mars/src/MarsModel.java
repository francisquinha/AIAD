import agents.Producer;
import agents.Spotter;
import agents.Transporter;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import sajas.sim.repast3.Repast3Launcher;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Network2DGridDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.network.DefaultDrawableNode;
import uchicago.src.sim.space.Object2DGrid;

/**
 *
 * @author diogo
 */
public class MarsModel extends Repast3Launcher {
    
    private ContainerController mainContainer;
    private Schedule schedule;
    private Object2DGrid space;
    private DisplaySurface displaySurface;
    private Object2DDisplay display;
    private List<DefaultDrawableNode> nodes;
    
    private Environment environment;
    private List<Producer> producers;
    private List<Spotter> spotters;
    private List<Transporter> transporters;
    
    public MarsModel(Environment environment) {
        this.environment = environment;
    }
    
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
        } catch(StaleProxyException e) {
            System.out.println("Could not launch the agents! " + e.getMessage());
        }
    }
    
    protected void buildAgents() throws StaleProxyException {
        this.nodes = new ArrayList<>();
        this.spotters = new ArrayList(this.environment.spotters);
        this.producers = new ArrayList(this.environment.producers);
        this.transporters = new ArrayList(this.environment.transporters);
        
        for(int i = 0; i < this.environment.spotters; i++) {
            Spotter spotter = new Spotter();
            this.spotters.add(spotter);
            this.mainContainer.acceptNewAgent("Spotter" + i, spotter).start();
        }
        
        for(int i = 0; i < this.environment.producers; i++) {
            Producer producer = new Producer();
            this.producers.add(producer);
            this.mainContainer.acceptNewAgent("Producer" + i, producer).start();
        }
        
        for(int i = 0; i < this.environment.transporters; i++) {
            Transporter transporter = new Transporter();
            this.transporters.add(transporter);
            this.mainContainer.acceptNewAgent("Transporter" + i, transporter).start();
        }
        
        this.spotters.forEach(spotter -> this.nodes.add(spotter.node));
        this.producers.forEach(producer -> this.nodes.add(producer.node));
        this.transporters.forEach(transporter -> this.nodes.add(transporter.node));
    }
    
    protected void buildSpace() {
        this.space = new Object2DGrid(this.environment.size, this.environment.size);
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
        int spaceSize = this.environment.size;
        int spottersCount = this.spotters.size();
        int height = spaceSize/spottersCount;
        int currentOffset = 0;
        
        for(Spotter spotter : this.spotters) {
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
