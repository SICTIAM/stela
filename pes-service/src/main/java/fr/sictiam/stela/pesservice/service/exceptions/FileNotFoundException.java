package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class FileNotFoundException extends RuntimeException {

    public FileNotFoundException() {
        super("notifications.pes.file_not_found");
    }
    public FileNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}