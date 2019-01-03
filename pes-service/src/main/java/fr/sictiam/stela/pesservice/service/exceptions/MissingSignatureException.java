package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingSignatureException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public MissingSignatureException() {
        super("Missing signature");
    }

    public MissingSignatureException(String message) {
        super(message);
    }

    public MissingSignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}

