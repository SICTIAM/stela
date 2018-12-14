package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class RecipientExistsException extends ConvocationException {

    public RecipientExistsException() {
        super("errors.recipient.alreadyExists");
    }
}
