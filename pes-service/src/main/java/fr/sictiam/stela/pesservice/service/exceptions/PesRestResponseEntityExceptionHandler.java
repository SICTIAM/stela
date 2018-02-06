package fr.sictiam.stela.pesservice.service.exceptions;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

public class PesRestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler({ PesNotFoundException.class })
    protected ResponseEntity<Object> handlePesNotFound(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Pes not found.", new HttpHeaders(), HttpStatus.NOT_FOUND, request);
    }

    @ExceptionHandler({ PesCreationException.class })
    protected ResponseEntity<Object> handlePesCreationException(Exception ex, WebRequest request) {
        return handleExceptionInternal(ex, "Pes creation error.", new HttpHeaders(),
                HttpStatus.INTERNAL_SERVER_ERROR, request);
    }
}