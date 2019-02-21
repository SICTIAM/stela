package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;

import javax.persistence.Embeddable;

@Embeddable
public class StampPosition {

    @JsonView(Views.LocalAuthority.class)
    private int x = 10;

    @JsonView(Views.LocalAuthority.class)
    private int y = 10;

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
