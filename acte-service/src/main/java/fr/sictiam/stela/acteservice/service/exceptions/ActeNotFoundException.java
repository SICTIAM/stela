package fr.sictiam.stela.acteservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND, reason = "notifications.acte.not_found")
public class ActeNotFoundException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public ActeNotFoundException() {
        super("notifications.acte.not_found");
    }
}