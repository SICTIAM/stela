package fr.sictiam.stela.convocationservice.model.csv;

public class PresenceBean {

    private String lastname;
    private String firstname;
    private String email;
    private String presence;
    private String guest;
    private String epci;

    public PresenceBean(String lastname, String firstname, String epci, String email, String presence,
            String guest) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.epci = epci;
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

    public String getEpci() {
        return epci;
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
        return new String[]{ "lastname", "firstname", "epci", "email", "presence", "guest" };
    }

    @Override public String toString() {
        return "{" +
                "\"lastname\": \"" + lastname + '\"' +
                ", \"firstname\": \"" + firstname + '\"' +
                ", \"epci\": \"" + epci + '\"' +
                ", \"email\": \"" + email + '\"' +
                ", \"presence\": \"" + presence + '\"' +
                ", \"guest\"" + guest + '\"' +
                '}';
    }
}
