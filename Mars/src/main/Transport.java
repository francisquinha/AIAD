package main;

import java.awt.*;

/**
 * Created by Angie.
 */
public class Transport {

    private final Point place;
    private final int quantity;

    public Transport(Point place, int quantity) {
        this.place = place;
        this.quantity = quantity;
    }

    public Point getPlace(){
        return this.place;
    }

    public int getQuantity() {
        return quantity;
    }

}
