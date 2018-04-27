package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PesSendException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public PesSendException() {
        super("notifications.pes.sent.error.send");
    }

    public PesSendException(String message, Throwable cause) {
        super(message, cause);
    }
}