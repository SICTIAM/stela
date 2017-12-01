package fr.sictiam.stela.acteservice.service.exceptions;

public class NoEnveloppeException extends RuntimeException {

    public NoEnveloppeException() {
        super("notifications.acte.no_enveloppe");
    }
    public NoEnveloppeException(String message, Throwable cause){
        super(message, cause);
    }
}

