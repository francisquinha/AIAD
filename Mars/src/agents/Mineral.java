/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package agents;

import java.awt.Color;
import java.util.concurrent.ThreadLocalRandom;
import main.Environment;
import main.MarsModel;

/**
 *
 * @author diogo
 */
public class Mineral extends MarsAgent {
    
    public Mineral(MarsModel model) {
        super(Color.PINK, model);
    }
    
    public void mine() {
        ThreadLocalRandom r = ThreadLocalRandom.current();
        int count = r.nextInt(Environment.MAX_FRAGMENTS_PER_MINERAL);
        MineralFragments fragments = new MineralFragments(this.model, count);
        this.model.addAgent(fragments, this.getPosition());
        this.model.removeAgent(this);
    }
    
}
