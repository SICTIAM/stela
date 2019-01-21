package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.UNAUTHORIZED)
public class TokenAuthenticationException extends ConvocationException {

    public TokenAuthenticationException() {
        super("Unauthorized");
    }
}
