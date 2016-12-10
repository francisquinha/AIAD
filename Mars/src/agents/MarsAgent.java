/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import java.awt.Color;
import java.awt.Point;
import java.util.ArrayList;
import main.MarsNode;
import sajas.core.Agent;
import sajas.domain.DFService;
import uchicago.src.sim.gui.Drawable2DGridNode;
import uchicago.src.sim.gui.Object2DDisplay;
import uchicago.src.sim.gui.SimGraphics;

/**
 *
 * @author diogo
 */
public class MarsAgent extends Agent implements Drawable2DGridNode {
    
    public static Point STARTING_POSITION = new Point(0, 0);
    public Object2DDisplay display = null;
    public final MarsNode node;
    
    protected MarsAgent(Color color) {
        node = new MarsNode(this);
        node.setColor(color);
    }
    
    public void translate(Point vector) {
        int newX = (int)this.node.getX() + vector.x;
        int newY = (int)this.node.getY() + vector.y;
        node.setX(newX);
        node.setY(newY);
    }
    
    public void move(Point position) {
        node.setX(position.x);
        node.setY(position.y);
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

    @Override
    public ArrayList getOutEdges() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getX() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getY() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void draw(SimGraphics sg) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    public static class Ontologies {
        public static final String SPOTTER = "Spotter";
        public static final String PRODUCER = "Producer";
        public static final String TRANSPORTER = "Transporter";
        public static final String MINERAL = "Mineral";
    }
}
