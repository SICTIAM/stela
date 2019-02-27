package fr.sictiam.stela.convocationservice.model.exception;

public class ConvocationNotAvailableException extends ConvocationException {

    public ConvocationNotAvailableException() {
        super("convocation.errors.convocation.notAvailable");
    }

    public ConvocationNotAvailableException(String msg) {
        super(msg);
    }

    public ConvocationNotAvailableException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
