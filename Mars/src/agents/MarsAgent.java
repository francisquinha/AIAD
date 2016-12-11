/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import jade.core.AID;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import main.MarsModel;
import main.MarsNode;
import sajas.core.Agent;
import sajas.domain.DFService;
import uchicago.src.sim.gui.NetworkDrawable;

import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

/**
 * @author diogo
 */
public class MarsAgent extends Agent implements Comparable {

    final MarsModel model;
    public final MarsNode node;

    MarsAgent(Color color, MarsModel model, NetworkDrawable drawable) {
        this.model = model;
        node = new MarsNode(this, drawable);
        node.setColor(color);
    }

    void translate(Point vector) {
        Point position = getPosition();
        position.translate(vector.x, vector.y);
        move(position);
    }

    public void move(Point position) {
        model.moveAgent(this, position);
    }

    public Point getPosition() {
        return new Point((int) node.getX(), (int) node.getY());
    }

    final AID[] getAgents(String ontology) {
        try {
            DFAgentDescription description = new DFAgentDescription();
            description.addOntologies(ontology);
            DFAgentDescription[] found = DFService.search(this, description);
            AID[] aids = new AID[found.length];

            for (int i = 0; i < found.length; i++)
                aids[i] = found[i].getName();

            return aids;
        } catch (FIPAException e) {
            System.out.println("No agents with the ontology " + ontology);
            return new AID[0];
        }
    }

    Queue<Point> getPlanToPosition(Point from, Point target, int maxDistance) {
        Queue<Point> plan = new LinkedList<>();
        while (Math.abs(from.distance(target)) > maxDistance) {
            int dx = target.x - from.x;
            int dy = target.y - from.y;
            Point nextMove;
            if (dx != 0) {
                dx = dx > 0 ? Math.min(1, dx) : Math.max(-1, dx);
                nextMove = new Point(dx, 0);
            } else {
                dy = dy > 0 ? Math.min(1, dy) : Math.max(-1, dy);
                nextMove = new Point(0, dy);
            }

            from.translate(nextMove.x, nextMove.y);
            plan.add(nextMove);
        }
        return plan;
    }

    @Override
    public int compareTo(Object t) {
        if (t == null)
            return 1;

        if (t instanceof MarsAgent)
            return toString().compareTo(t.toString());
        else
            return -1;
    }

    public static class Ontologies {
        public static final String SPOTTER = "Spotter";
        public static final String PRODUCER = "Producer";
        public static final String TRANSPORTER = "Transporter";
        public static final String MINERAL = "Mineral";
    }
}
