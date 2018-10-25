package fr.sictiam.stela.acteservice.model.ui;

import org.springframework.http.HttpStatus;
import org.springframework.validation.ObjectError;

import java.util.Arrays;
import java.util.Collections;
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
        this(errors, message, HttpStatus.BAD_REQUEST);
    }

    public CustomValidationUI(List<ObjectError> errors, String message, HttpStatus status) {
        this.timestamp = new Date().getTime();
        this.status = status.value();
        this.error = status.getReasonPhrase();
        this.errors = errors;
        this.message = message;
    }

    public CustomValidationUI(ObjectError error, String message, HttpStatus status) {
        this(Collections.singletonList(error), message, status);
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