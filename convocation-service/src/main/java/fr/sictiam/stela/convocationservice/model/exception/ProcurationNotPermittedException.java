package fr.sictiam.stela.convocationservice.model.exception;

public class ProcurationNotPermittedException extends ConvocationException {

    public ProcurationNotPermittedException() {
        super("Recipient is a guest, procuration is not permitted");
    }

    public ProcurationNotPermittedException(String msg) {
        super(msg);
    }

    public ProcurationNotPermittedException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
