package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class HistoryNotFoundException extends RuntimeException {

    public HistoryNotFoundException() {
        super("notifications.pes.history_not_found");
    }

    public HistoryNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}