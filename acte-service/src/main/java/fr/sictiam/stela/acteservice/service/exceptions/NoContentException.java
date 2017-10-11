package fr.sictiam.stela.acteservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NO_CONTENT)
public class NoContentException extends RuntimeException {

    public NoContentException() {
        super("notifications.acte.no_content");
    }
    public NoContentException(String message, Throwable cause){
        super(message, cause);
    }
}
