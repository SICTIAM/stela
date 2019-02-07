package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class ConvocationFileException extends ConvocationException {

    public ConvocationFileException(String msg) {
        super(msg);
    }

    public ConvocationFileException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
