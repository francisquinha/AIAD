/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import main.Environment;
import sajas.core.Agent;
import sajas.domain.DFService;
import uchicago.src.sim.gui.RoundRectNetworkItem;
import uchicago.src.sim.network.DefaultDrawableNode;

import java.awt.*;

/**
 *
 * @author diogo
 */
public class MarsAgent extends Agent {
    
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
        item.setHeight(Environment.CELL_SIZE);
        item.setWidth(Environment.CELL_SIZE);
        
        return item;
    }
    
    public AID[] getAgents(String ontology) {
        try {
            DFAgentDescription description = new DFAgentDescription();
            description.addOntologies(ontology);
            DFAgentDescription[] found = DFService.search(this, description);
            AID[] aids = new AID[found.length];
            
            for(int i = 0; i < found.length;  i++)
                aids[i] = found[i].getName();
            
            return aids;
        } catch(FIPAException e) {
            System.out.println("No agents with the ontology " + ontology);
            return new AID[0];
        }
    }
    
    public static class Ontologies {
        public static final String SPOTTER = "Spotter";
        public static final String PRODUCER = "Producer";
        public static final String TRANSPORTER = "Transporter";
    }

    public Point getPosition() {
        return new Point((int)this.node.getX(), (int)this.node.getY());
    }

}
