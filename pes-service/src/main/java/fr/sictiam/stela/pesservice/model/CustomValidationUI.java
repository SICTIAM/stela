package fr.sictiam.stela.pesservice.model;

import org.springframework.validation.ObjectError;

import java.util.Date;
import java.util.List;

public class CustomValidationUI {

    long timestamp;
    int status;
    String error;
    List<ObjectError> errors;
    String message;

    // reproduce bad request return
    public CustomValidationUI(List<ObjectError> errors, String message) {
        this.timestamp = new Date().getTime();
        this.status = 400;
        this.error = "Bad Request";
        this.errors = errors;
        this.message = message;

    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getError() {
        return error;
    }

    public List<ObjectError> getErrors() {
        return errors;
    }

    public String getMessage() {
        return message;
    }

}