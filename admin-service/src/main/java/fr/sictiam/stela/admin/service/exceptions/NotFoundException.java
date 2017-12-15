package fr.sictiam.stela.admin.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException {

    public NotFoundException() {
        super("notifications.admin.not_found");
    }
    public NotFoundException(String message){
        super(message);
    }
}