package fr.sictiam.stela.acteservice.service.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public class ActeRestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ ActeNotFoundException.class })
    protected ResponseEntity<Object> handleActeNotFound(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Acte not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ ActeNotSentException.class })
    protected ResponseEntity<Object> handleActeNotSent(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Acte not sent.", new HttpHeaders(), HttpStatus.INTERNAL_SERVER_ERROR,
                request);
    }

    @ExceptionHandler({ FileNotFoundException.class })
    protected ResponseEntity<Object> AnnexeNotFound(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Annexe not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ HistoryNotFoundException.class })
    protected ResponseEntity<Object> HistoryNotFound(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "History not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ FileNotFoundException.class })
    protected ResponseEntity<Object> NoHistoryFile(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "No history file.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ CancelForbiddenException.class })
    protected ResponseEntity<Object> CancelForbidden(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "This acte cannot be cancelled", new HttpHeaders(), HttpStatus.FORBIDDEN,
                request);
    }

    @ExceptionHandler({ NoContentException.class })
    protected ResponseEntity<Object> NoContent(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "There is no additional content to send in the response payload body",
                new HttpHeaders(), HttpStatus.NO_CONTENT, request);
    }
}