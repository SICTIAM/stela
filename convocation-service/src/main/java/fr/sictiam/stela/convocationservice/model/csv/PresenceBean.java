package fr.sictiam.stela.convocationservice.model.csv;

public class PresenceBean {

    private String lastname;
    private String firstname;
    private String email;
    private String presence;
    private String guest;

    public PresenceBean(String lastname, String firstname, String email, String presence, String guest) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.email = email;
        this.presence = presence;
        this.guest = guest;
    }

    public String getLastname() {
        return lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getEmail() {
        return email;
    }

    public String getPresence() {
        return presence;
    }

    public String getGuest() {
        return guest;
    }

    public static String[] fields() {
        return new String[]{ "lastname", "firstname", "email", "presence", "guest" };
    }

    @Override public String toString() {
        return "PresenceBean{" +
                "lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", email='" + email + '\'' +
                ", presence='" + presence + '\'' +
                ", guest='" + guest + '\'' +
                '}';
    }
}
