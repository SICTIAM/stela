package fr.sictiam.stela.acteservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class CertificateAuthenticationFailedException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public CertificateAuthenticationFailedException() {
        super("Authentication failed");
    }

    public CertificateAuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}