package fr.sictiam.stela.admin.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class LocalAuthorityException extends RuntimeException {

    public LocalAuthorityException() {
        super("notifications.admin.local_authority_not_found");
    }
    public LocalAuthorityException(String message, Throwable cause){
        super(message, cause);
    }
}