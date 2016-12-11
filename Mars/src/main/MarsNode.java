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
    
    MarsAgent agent;
    
    public MarsNode(MarsAgent agent) {
        super (new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        this.agent = agent;
    }
/*
    public static MarsNode makeMarsNode(MarsAgent agent) {


        if (agent.ontology == MarsAgent.Ontologies.SHIP)
            return new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y);
        else
            return new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y);

    }

    private */
    
}
/*
private class ShipDrawable extends DefaultDrawableNode {
    private ShipDrawable() {
        super(new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
    }
}

private class MovingDrawable extends DefaultDrawableNode {
    private MovingDrawable() {
        super(new OvalNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
    }
}*/