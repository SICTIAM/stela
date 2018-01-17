package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class PesNotFoundException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public PesNotFoundException() {
        super("notifications.acte.not_found");
    }
    public PesNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}