package fr.sictiam.stela.acteservice.service.exceptions;

public class ActeNotSentException extends RuntimeException {

    static final long serialVersionUID = 43L;

    public ActeNotSentException(String message) {
        super(message);
    }
    public ActeNotSentException(String message, Throwable cause){
        super(message, cause);
    }
}