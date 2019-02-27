package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Recipient;

public class RecipientUI {

    protected String uuid;

    protected String firstname;

    protected String lastname;

    public RecipientUI(Recipient recipient) {
        uuid = recipient.getUuid();
        firstname = recipient.getFirstname();
        lastname = recipient.getLastname();
    }

    public String getUuid() {
        return uuid;
    }

    public String getFirstname() {
        return firstname;
    }

    public String getLastname() {
        return lastname;
    }

}
