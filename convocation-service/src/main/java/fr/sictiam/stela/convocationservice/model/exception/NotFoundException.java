package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends ConvocationException {

    public NotFoundException() {
        this("Not found");
    }

    public NotFoundException(String message) {
        super(message);
    }
}
