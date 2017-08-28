package fr.sictiam.stela.acteservice.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class AnnexeNotFoundException extends RuntimeException {

    public AnnexeNotFoundException() {
        super("notifications.acte.annexe_not_found");
    }
    public AnnexeNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}