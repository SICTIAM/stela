package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.CONFLICT)
public class RecipientExistsException extends ConvocationException {

    public RecipientExistsException() {
        this("convocation.errors.recipient.alreadyExists");
    }

    public RecipientExistsException(String message) {
        super(message);
    }
}
