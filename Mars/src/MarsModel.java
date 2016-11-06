
import uchicago.src.sim.engine.Schedule;
import uchicago.src.sim.engine.SimInit;
import uchicago.src.sim.engine.SimModelImpl;
import uchicago.src.sim.space.Multi2DGrid;

/**
 *
 * @author diogo
 */
public class MarsModel extends SimModelImpl {
    
    public MarsModel() {
        Multi2DGrid space = new Multi2DGrid(100, 100, true);
        Producer agent = new Producer();
        space.putObjectAt(1, 1, agent);
    }

    @Override
    public String[] getInitParam() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void begin() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setup() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Schedule getSchedule() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getName() {
        return "Mars Model";
    }
    
    public static void main(String[] args) {
        SimInit init = new SimInit();
	init.loadModel(new MarsModel(), null, false);
    }
}
