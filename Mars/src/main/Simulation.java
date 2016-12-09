package main;

import sajas.sim.repast3.Repast3Launcher;
import uchicago.src.sim.engine.SimInit;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
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
