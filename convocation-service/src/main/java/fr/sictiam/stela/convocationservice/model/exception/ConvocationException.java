package fr.sictiam.stela.convocationservice.model.exception;

public class ConvocationException extends RuntimeException {

    public ConvocationException(String msg) {
        super(msg);
    }

    public ConvocationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
