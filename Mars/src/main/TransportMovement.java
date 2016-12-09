package main;

import java.awt.*;

/**
 * Created by Angie.
 */
public class TransportMovement extends Transport {

    private Movement movement2Place;
    private Movement movement2Ship;

    public TransportMovement(Point place, int quantity, Point transporterPosition, Point shipPosition) {
        super(place, quantity);
        movement2Place = new Movement(transporterPosition, place);
        movement2Ship = new Movement(place, shipPosition);
    }

    public int getCost() {
        return movement2Place.getSteps() + movement2Ship.getSteps();
    }

    public int getOneWayCost() {
        return movement2Place.getSteps();
    }

    public Movement getMovement2Place() {
        return movement2Place;
    }

    public Movement getMovement2Ship() {
        return movement2Ship;
    }

}

