package fr.sictiam.stela.convocationservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ConvocationNotFoundException extends RuntimeException {

    public ConvocationNotFoundException() {
        super("notifications.convocation.not_found");
    }

    public ConvocationNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}