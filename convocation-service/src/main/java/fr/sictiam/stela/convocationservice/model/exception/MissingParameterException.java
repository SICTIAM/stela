package fr.sictiam.stela.convocationservice.model.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST)
public class MissingParameterException extends ConvocationException {

    private String parameter;

    public MissingParameterException() {
        this("Undefined");
    }

    public MissingParameterException(String parameter) {

        super("errors.validation.missingParameter");
        this.parameter = parameter;
    }

    public String getParameter() {
        return parameter;
    }
}
