package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidEmailAddressException extends ConvocationException {

    public InvalidEmailAddressException() {
        super("convocation.errors.recipient.invalidEmail");
    }
}
