package fr.sictiam.stela.acteservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class ActeNotFoundException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public ActeNotFoundException() {
        super();
    }
    public ActeNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}