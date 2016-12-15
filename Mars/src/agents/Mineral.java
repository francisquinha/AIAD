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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author diogo
 */
public class Mineral extends MarsAgent {

    private final int quantity;
    private final boolean extractable;
    final AtomicInteger fragments;
    final AtomicInteger previewFragments;

    public Mineral(MarsModel model) {
        super(Color.PINK, model, new RectNetworkItem(Environment.SHIP_POSITION.x, Environment.SHIP_POSITION.y));
        ThreadLocalRandom r = ThreadLocalRandom.current();
        quantity = r.nextInt(Environment.MIN_FRAGMENTS_PER_MINERAL, Environment.MAX_FRAGMENTS_PER_MINERAL);
        extractable = getRandomBoolean(r, Environment.PROB_EXTRACTABLE_MINERAL);
        fragments = new AtomicInteger(quantity);
        previewFragments = new AtomicInteger(quantity);
    }

    private Boolean getRandomBoolean(ThreadLocalRandom r, int probability) {
        int randomInt = r.nextInt(100);
        return randomInt < probability;
    }

    int getQuantity() {
        return quantity;
    }

    public boolean getExtractable() {
        return extractable;
    }

    int take(int wanted) {
        if (wanted >= fragments.get()) {
            model.removeAgent(this);
            return fragments.get();
        } else {
            fragments.getAndAdd(-wanted);
            return wanted;
        }
    }

    void previewTake(int wanted) {
        previewFragments.getAndAdd(-Math.min(wanted, previewFragments.get()));
    }

}
