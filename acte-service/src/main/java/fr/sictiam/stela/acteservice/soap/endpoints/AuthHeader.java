package fr.sictiam.stela.acteservice.soap.endpoints;

public class AuthHeader {

    private String serial;
    private String vendor;

    public AuthHeader(String serial, String vendor) {
        super();
        this.serial = serial;
        this.vendor = vendor;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    @Override
    public String toString() {
        return "AuthHeader [serial=" + serial + ", vendor=" + vendor + "]";
    }
}
