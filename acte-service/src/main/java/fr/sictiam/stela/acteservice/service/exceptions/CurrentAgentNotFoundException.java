package fr.sictiam.stela.acteservice.service.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class CurrentAgentNotFoundException extends RuntimeException {

    static final long serialVersionUID = 42L;

    public CurrentAgentNotFoundException() {
        super("notifications.agent.not_found");
    }
    public CurrentAgentNotFoundException(String message, Throwable cause){
        super(message, cause);
    }
}