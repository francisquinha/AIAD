/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import java.awt.Color;
import java.awt.Point;
import sajas.core.Agent;
import uchicago.src.sim.gui.RoundRectNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

/**
 *
 * @author diogo
 */
public class MarsAgent extends Agent {
    
    public static int NODE_SIZE = 10;
    public static Point STARTING_POSITION = new Point(0, 0);
    
    public DefaultDrawableNode node;
    
    protected MarsAgent(Color color) {
        RoundRectNetworkItem item = MarsAgent.createDefaultItem();
        this.node = new DefaultDrawableNode(item);
        this.node.setColor(color);
    }
    
    private static RoundRectNetworkItem createDefaultItem() {
        RoundRectNetworkItem item = new RoundRectNetworkItem(STARTING_POSITION.x, STARTING_POSITION.y);
        item.allowResizing(false);
        item.setHeight(MarsAgent.NODE_SIZE);
        item.setWidth(MarsAgent.NODE_SIZE);
        
        return item;
    }
    
}
