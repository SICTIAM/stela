package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.controller.PaullGenericController;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class PaullService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaullGenericController.class);

    private ExternalRestService externalRestService;

    public PaullService(ExternalRestService externalRestService) {
        this.externalRestService = externalRestService;
    }

    public GenericAccount emailAuth(String email, String password) {
        try {
            return externalRestService.authWithEmailPassword(email, password);
        } catch (RuntimeException e) {
            LOGGER.error("[emailAuth] Authentication with generic account {} failed: {}", email, e.getMessage());
            return null;
        }
    }
}
