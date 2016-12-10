/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import java.awt.Color;
import main.MarsModel;

/**
 *
 * @author diogo
 */
public class MineralFragments extends MarsAgent {
    
    public int quantity;
    
    public MineralFragments(MarsModel model, int quantity) {
        super(Color.MAGENTA, model);
        this.quantity = quantity;
        this.node.setLabelColor(Color.WHITE);
        this.node.setNodeLabel("" + quantity);
    }
    
    public int take(int wanted) {
        if(wanted >= this.quantity) {
            this.model.removeAgent(this);
            return this.quantity;
        } else {
            this.quantity -= wanted;
            return wanted;
        }
    }
    
}
