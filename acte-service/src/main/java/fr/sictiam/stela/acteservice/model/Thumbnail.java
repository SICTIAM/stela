package fr.sictiam.stela.acteservice.model;

public class Thumbnail {

    OrientationEnum orientation;

    String image;

    public Thumbnail(){}

    public Thumbnail(OrientationEnum orientation, String image) {
        this.orientation = orientation;
        this.image = image;
    }

    public OrientationEnum getOrientation() {
        return orientation;
    }

    public void setOrientation(OrientationEnum orientation) {
        this.orientation = orientation;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

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


}


