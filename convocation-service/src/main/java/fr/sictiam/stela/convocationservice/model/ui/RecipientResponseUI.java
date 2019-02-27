package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.ResponseType;

public class RecipientResponseUI extends RecipientUI {

    private ResponseType response;

    public RecipientResponseUI(RecipientResponse recipientResponse) {
        super(recipientResponse.getRecipient());
        response = recipientResponse.getResponseType();
    }

    public ResponseType getResponse() {
        return response;
    }
}
