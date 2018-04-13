package fr.sictiam.stela.pesservice.model.migration;

public class UserMigration {

    private String name;
    private String uname;
    private String email;

    public UserMigration() {
    }

    public UserMigration(String name, String uname, String email) {
        this.name = name;
        this.uname = uname;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUname() {
        return uname;
    }

    public void setUname(String uname) {
        this.uname = uname;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "UserMigration{" +
                "name='" + name + '\'' +
                ", uname='" + uname + '\'' +
                ", email='" + email + '\'' +
                '}';
    }
}
