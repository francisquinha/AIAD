import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.StaleProxyException;
import java.util.ArrayList;
import java.util.List;
import sajas.sim.repast3.Repast3Launcher;
import sajas.core.Runtime;
import sajas.wrapper.ContainerController;

/**
 *
 * @author diogo
 */
public class MarsModel extends Repast3Launcher {
    
    private ContainerController mainContainer;
    private List<Producer> producers = new ArrayList<>();
    private List<Spotter> spotters = new ArrayList<>();
    
    @Override
    public void setup() {
        super.setup();
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
        this.spotters.add(mainSpotter);
        this.mainContainer.acceptNewAgent("MainSpotter", mainSpotter).start();
        
        for(int i = 0; i < 5; i++) {
            Producer newProducer = new Producer();
            this.producers.add(newProducer);
            this.mainContainer.acceptNewAgent("Producer" + i, newProducer).start();
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
