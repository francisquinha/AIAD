/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package main;

import agents.MarsAgent;
import uchicago.src.sim.gui.NetworkDrawable;
import uchicago.src.sim.network.DefaultDrawableNode;
/**
 *
 * @author diogo
 */
public class MarsNode extends DefaultDrawableNode {

    MarsAgent agent;

    public MarsNode(MarsAgent agent, NetworkDrawable drawable) {
        super(drawable);
        this.agent = agent;
    }


}
