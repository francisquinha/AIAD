package agents;


import behaviours.LogBehaviour;
import java.awt.Color;


/**
 *
 * @author diogo
 */
public class Transporter extends MarsAgent {
    
    public Transporter() {
        super(Color.BLUE);
    }
    
    @Override
    protected void setup() {
        this.addBehaviour(new LogBehaviour(this));
        System.out.println("A transporter was set up!");
    }
    
}
