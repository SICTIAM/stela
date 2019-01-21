package fr.sictiam.stela.acteservice.model.util;

public enum OrientationEnum {

    LANDSCAPE("landscape"),
    PORTRAIT("portrait");

    private final String direction;

    OrientationEnum(String direction) {
        this.direction = direction;
    }

    public String toString() {
        return direction;
    }
}

