package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class InvalidParameterException extends ConvocationException {

    private String parameter;

    public InvalidParameterException() {
        this("Undefined");
    }

    public InvalidParameterException(String parameter) {

        super("convocation.errors.convocation.invalidParameter");
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
