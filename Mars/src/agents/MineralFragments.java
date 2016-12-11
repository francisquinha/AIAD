/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import java.awt.Color;
import java.util.concurrent.atomic.AtomicInteger;
import main.MarsModel;

/**
 *
 * @author diogo
 */
public class MineralFragments extends MarsAgent {
    
    public AtomicInteger quantity;
    
    public MineralFragments(MarsModel model, int quantity) {
        super(Color.MAGENTA, model);
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
