package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ProfileNotConfiguredForSesileException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public ProfileNotConfiguredForSesileException() {
        super("notifications.pes.sesile.not_configured");
    }

    public ProfileNotConfiguredForSesileException(String message, Throwable cause) {
        super(message, cause);
    }
}