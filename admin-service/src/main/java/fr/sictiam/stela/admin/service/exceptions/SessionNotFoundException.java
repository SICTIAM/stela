package fr.sictiam.stela.admin.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class SessionNotFoundException extends RuntimeException {

    public SessionNotFoundException() {
        super("notifications.admin.session_not_found");
    }

    public SessionNotFoundException(String message) {
        super(message);
    }
}