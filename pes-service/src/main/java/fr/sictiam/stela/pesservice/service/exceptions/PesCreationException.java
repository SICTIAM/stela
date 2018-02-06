package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PesCreationException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public PesCreationException() {
        super("notifications.pes.sent.error.non_extractable_pes");
    }

    public PesCreationException(String message, Throwable cause) {
        super(message, cause);
    }
}