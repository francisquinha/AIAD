/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import agents.MarsAgent;
import uchicago.src.sim.gui.OvalNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;
/**
 *
 * @author diogo
 */
public class MarsNode extends DefaultDrawableNode {
    
    public MarsAgent agent;
    
    public MarsNode(MarsAgent agent) {
        super(new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        this.agent = agent;
    }
    
}
