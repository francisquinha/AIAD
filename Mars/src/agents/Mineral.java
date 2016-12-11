/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import main.Environment;
import main.MarsModel;
import uchicago.src.sim.gui.RectNetworkItem;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author diogo
 */
public class Mineral extends MarsAgent {

	private int mineralFrag;
	
    public Mineral(MarsModel model) {
        super(Color.PINK, model, new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        ThreadLocalRandom r = ThreadLocalRandom.current();
        mineralFrag = r.nextInt(Environment.MIN_FRAGMENTS_PER_MINERAL, Environment.MAX_FRAGMENTS_PER_MINERAL);
    }

    MineralFragments mine() {
        MineralFragments fragments = new MineralFragments(model, mineralFrag);
        model.addAgent(fragments, getPosition());
        model.removeAgent(this);
        return fragments;
    }
    
    int getMineralFrag(){
    	return mineralFrag;
    }

}
