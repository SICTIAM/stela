package fr.sictiam.stela.convocationservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.SERVICE_UNAVAILABLE)
public class StorageException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public StorageException() {
        super("storage error");
    }

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}