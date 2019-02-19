package fr.sictiam.stela.convocationservice.model.exception;

public class ConvocationCancelledException extends ConvocationException {

    public ConvocationCancelledException() {
        super("Convocation cancelled");
    }

    public ConvocationCancelledException(String msg) {
        super(msg);
    }

    public ConvocationCancelledException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
