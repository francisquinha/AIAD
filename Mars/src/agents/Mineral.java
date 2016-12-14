/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import main.Environment;
import main.MarsModel;
import uchicago.src.sim.gui.RectNetworkItem;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * @author diogo
 */
public class Mineral extends MarsAgent {

    private final int quantity;
    private final boolean extractable;

    public Mineral(MarsModel model) {
        super(Color.PINK, model, new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        ThreadLocalRandom r = ThreadLocalRandom.current();
        quantity = r.nextInt(Environment.MIN_FRAGMENTS_PER_MINERAL, Environment.MAX_FRAGMENTS_PER_MINERAL);

        extractable = getRandomBoolean(r, Environment.PROBABILITY_EXTRACTABLE);
    }

    private Boolean getRandomBoolean(ThreadLocalRandom r, int probability) {
        int randomInt = r.nextInt(100);
        return randomInt < probability;
    }

    MineralFragments mine() {
        MineralFragments fragments = new MineralFragments(model, quantity);
        model.addAgent(fragments, getPosition());
        model.removeAgent(this);
        return fragments;
    }

    int getQuantity() {
        return quantity;
    }

    public boolean getExtractable() {
        return extractable;
    }

}
