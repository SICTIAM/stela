package fr.sictiam.signature.pes.verifier;

public class InvalidPesAllerFileException extends Exception {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public InvalidPesAllerFileException(String message) {
        super();
    }

    public InvalidPesAllerFileException(Throwable cause) {
        super();
    }

    public InvalidPesAllerFileException(String message, Throwable cause) {
        super(cause);
    }
}