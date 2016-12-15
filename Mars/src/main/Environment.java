package main;

import java.awt.*;

/**
 * @author diogo
 */
public class Environment {

    public static final int SIZE = 20;
    static final int MINERALS = 10;

    static final int SPOTTERS = 5;
    static final int PRODUCERS = 5;
    static final int TRANSPORTERS = 5;

    public static final int PROB_EXTRACTABLE_MINERAL = 60;
    public static final int MIN_FRAGMENTS_PER_MINERAL = 5;
    public static final int MAX_FRAGMENTS_PER_MINERAL = 15;

    public static final int TRANSPORTER_CAPACITY = 10;

    public static final int MINING_DISTANCE = 0;

    public static final Point SHIP_POSITION = new Point(SIZE / 2, SIZE / 2);

}
