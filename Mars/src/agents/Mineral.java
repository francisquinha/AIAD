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
 *
 * @author diogo
 */
public class Mineral extends MarsAgent {
    
    public Mineral(MarsModel model) {
        super(Color.PINK, model, new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
    }
    
    public MineralFragments mine() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int count = r.nextInt(Environment.MIN_FRAGMENTS_PER_MINERAL, Environment.MAX_FRAGMENTS_PER_MINERAL);
        MineralFragments fragments = new MineralFragments(this.model, count);
        this.model.addAgent(fragments, this.getPosition());
        this.model.removeAgent(this);
        return fragments;
    }
    
}
