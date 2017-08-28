package fr.sictiam.stela.acteservice.controller;

import fr.sictiam.stela.acteservice.controller.exceptions.ActeNotFoundException;
import fr.sictiam.stela.acteservice.controller.exceptions.AnnexeNotFoundException;
import fr.sictiam.stela.acteservice.controller.exceptions.HistoryNotFoundException;
import fr.sictiam.stela.acteservice.controller.exceptions.NoHistoryFileException;
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

    @ExceptionHandler({AnnexeNotFoundException.class})
    protected ResponseEntity<Object> AnnexeNotFound(Exception ex, WebRequest request){
        return handleExceptionInternal(ex, "Annexe not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({HistoryNotFoundException.class})
    protected ResponseEntity<Object> HistoryNotFound(Exception ex, WebRequest request){
        return handleExceptionInternal(ex, "History not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({NoHistoryFileException.class})
    protected ResponseEntity<Object> NoHistoryFile(Exception ex, WebRequest request){
        return handleExceptionInternal(ex, "No history file.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }
}