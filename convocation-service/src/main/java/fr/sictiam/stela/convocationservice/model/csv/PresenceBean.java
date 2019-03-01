package fr.sictiam.stela.convocationservice.model.csv;

public class PresenceBean {

    private String lastname;
    private String firstname;
    private String email;
    private String presence;

    public PresenceBean(String lastname, String firstname, String email, String presence) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.email = email;
        this.presence = presence;
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

    public static String[] fields() {
        return new String[]{ "lastname", "firstname", "email", "presence" };
    }

    @Override public String toString() {
        return "PresenceBean{" +
                "lastname='" + lastname + '\'' +
                ", firstname='" + firstname + '\'' +
                ", email='" + email + '\'' +
                ", presence='" + presence + '\'' +
                '}';
    }
}
