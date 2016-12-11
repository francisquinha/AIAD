package main;

import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.engine.SimInit;

/**
 * @author diogo
 */
public class Simulation {

    public static void main(String[] args) {
        SimInit init = new SimInit();
        init.setNumRuns(1);
        Repast3Launcher model = new MarsModel();
        init.loadModel(model, null, false);
    }

}
