package fr.sictiam.stela.acteservice.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import fr.sictiam.stela.acteservice.service.ActeNotSentException;

public class ActeRestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler{

    @ExceptionHandler({ActeNotFoundException.class})
    protected ResponseEntity<Object> handleActeNotFound(Exception ex, WebRequest request){
        return handleExceptionInternal(ex, "Acte not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ActeNotSentException.class})
    protected ResponseEntity<Object> handleActeNotSent(Exception ex, WebRequest request){
        return handleExceptionInternal(ex, "Acte not sent.", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}