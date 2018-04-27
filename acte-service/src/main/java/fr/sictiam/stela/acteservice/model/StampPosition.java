package fr.sictiam.stela.acteservice.model;

import javax.persistence.*;

@Embeddable
public class StampPosition {
    private int x;
    private int y;

    public StampPosition() {
    }

    public StampPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}
