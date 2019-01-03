package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class SignatureException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public SignatureException() {
        super("Signature error");
    }

    public SignatureException(String message) {
        super(message);
    }

    public SignatureException(String message, Throwable cause) {
        super(message, cause);
    }
}

