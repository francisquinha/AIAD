/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package behaviours;

import agents.Spotter;
import sajas.core.behaviours.CyclicBehaviour;

/**
 *
 * @author diogo
 */
public class SpotterScanBehaviour extends CyclicBehaviour {

    private final Spotter spotter;
    
    public SpotterScanBehaviour(Spotter spotter) {
        this.spotter = spotter;
    }
    
    @Override
    public void action() {
        this.spotter.node.setX(this.spotter.node.getX() + 1);
    }
    
}
