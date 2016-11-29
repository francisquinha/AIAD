import agents.Producer;
import agents.Spotter;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import sajas.sim.repast3.Repast3Launcher;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.gui.DisplaySurface;
import uchicago.src.sim.gui.Drawable2DNode;
import uchicago.src.sim.gui.Network2DGridDisplay;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.gui.RectNetworkItem;
import uchicago.src.sim.gui.RoundRectNetworkItem;
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
    
    private List<Producer> producers = new ArrayList<>();
    private List<Spotter> spotters = new ArrayList<>();
    
    @Override
    public void setup() {
        super.setup();
        this.nodes = new ArrayList<>();
        DefaultDrawableNode node = generateNode(Color.RED);
        this.nodes.add(node);
        
        this.space = new Object2DGrid(15, 15);
        this.displaySurface = new DisplaySurface(this, "Mars");
        this.registerDisplaySurface("Mars", this.displaySurface);
        this.buildDisplay();
    }
    
    protected void buildDisplay() {
        this.display = new Network2DGridDisplay(this.space);
        this.display.setObjectList(this.nodes);
        this.displaySurface.addDisplayableProbeable(this.display, "Mars");
        addSimEventListener(this.displaySurface);
        this.displaySurface.display();
        getSchedule().scheduleActionAtInterval(1, this.displaySurface, "updateDisplay", Schedule.LAST);
    }
    
    @Override
    protected void launchJADE() {
        Runtime rt = Runtime.instance();
        Profile p1 = new ProfileImpl();
        this.mainContainer = rt.createMainContainer(p1);

        try {
            launchAgents();
        } catch(StaleProxyException e) {
            System.out.println("Could not launch the agents! " + e.getMessage());
        }
    }
    
    private void launchAgents() throws StaleProxyException {
        Spotter mainSpotter = new Spotter(); 
        this.mainContainer.acceptNewAgent("MainSpotter", mainSpotter).start();
        
        for(int i = 0; i < 5; i++) {
            Producer newProducer = new Producer();
            this.producers.add(newProducer);
            this.mainContainer.acceptNewAgent("Producer" + i, newProducer).start();
        }
    }

    private DefaultDrawableNode generateNode(Color color) {
        RoundRectNetworkItem item = new RoundRectNetworkItem(10, 10);
        item.allowResizing(false);
        item.setHeight(5);
        item.setWidth(5);
        
        DefaultDrawableNode node = new DefaultDrawableNode(item);
        node.setColor(color);
        
        return node;
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
