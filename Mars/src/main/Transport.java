package main;

import java.awt.geom.Point2D;

/**
 * Created by Angie.
 */
public class Transport {

    private final Point2D.Double place;
    private final int quantity;

    public Transport(Point2D.Double place, int quantity) {
        this.place = place;
        this.quantity = quantity;
    }

    public Point2D.Double getPlace(){
        return this.place;
    }

    public int getQuantity() {
        return quantity;
    }

}
