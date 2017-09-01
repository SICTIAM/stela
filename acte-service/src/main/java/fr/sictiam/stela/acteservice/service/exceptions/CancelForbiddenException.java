package fr.sictiam.stela.acteservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.FORBIDDEN)
public class CancelForbiddenException extends RuntimeException {

    public CancelForbiddenException() {
        super("notifications.acte.cancelled.forbidden");
    }
    public CancelForbiddenException(String message, Throwable cause){
        super(message, cause);
    }
}