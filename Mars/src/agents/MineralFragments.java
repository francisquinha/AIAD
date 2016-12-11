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
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * @author diogo
 */
public class MineralFragments extends MarsAgent {
    
    public AtomicInteger quantity;
    
    public MineralFragments(MarsModel model, int quantity) {
        super(Color.MAGENTA, model, new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        this.quantity = new AtomicInteger(quantity);
    }
    
    public int take(int wanted) {
        if(wanted >= this.quantity.get()) {
            this.model.removeAgent(this);
            return this.quantity.get();
        } else {
            this.quantity.getAndAdd(-wanted);
            return wanted;
        }
    }
    
}
