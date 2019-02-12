package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AccessNotGrantedException extends ConvocationException {

    public AccessNotGrantedException() {
        this("Access not granted");
    }

    public AccessNotGrantedException(String message) {
        super(message);
    }
}
