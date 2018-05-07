package fr.sictiam.stela.acteservice.service.util;

import fr.sictiam.stela.acteservice.model.util.AuthorizationContextClasses;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CertUtilService {

    @Value("${application.certVerificationEnabled}")
    boolean certVerificationEnabled;

    public boolean checkCert(String acr) {
        return !certVerificationEnabled || AuthorizationContextClasses.EIDAS_SUBSTANTIAL.getValue().equals(acr);
    }
}