package fr.sictiam.stela.acteservice.controller.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NoHistoryFileException extends RuntimeException {

    public NoHistoryFileException() {
        super("notifications.acte.no_history_file");
    }
    public NoHistoryFileException(String message, Throwable cause){
        super(message, cause);
    }
}