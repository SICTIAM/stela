package fr.sictiam.stela.convocationservice.model.csv;

public class RecipientBean {

    private String lastname;
    private String firstname;
    private String epci;
    private String email;
    private String phoneNumber;

    public RecipientBean() {
    }

    public RecipientBean(String lastname, String firstname, String epci, String email, String phoneNumber) {
        this.lastname = lastname;
        this.firstname = firstname;
        this.epci = epci;
        this.email = email;
        this.phoneNumber = phoneNumber;
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

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setEpci(String epci) {
        this.epci = epci;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public static String[] fields() {
        return new String[]{ "lastname", "firstname", "epci", "email", "phoneNumber" };
    }

    @Override public String toString() {
        return "{" +
                "\"lastname\": \"" + lastname + '\"' +
                ", \"firstname\": \"" + firstname + '\"' +
                ", \"epci\": \"" + epci + '\"' +
                ", \"email\": \"" + email + '\"' +
                ", \"phoneNumber\": \"" + phoneNumber + '\"' +
                '}';
    }
}
