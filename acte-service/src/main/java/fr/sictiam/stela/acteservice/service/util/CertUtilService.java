package fr.sictiam.stela.acteservice.service.util;

import fr.sictiam.stela.acteservice.model.util.CertificateStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class CertUtilService {

    @Value("${application.certVerificationEnabled}")
    boolean certVerificationEnabled;

    public boolean checkCert(CertificateStatus certificateStatus) {
        return !certVerificationEnabled || CertificateStatus.VALID.equals(certificateStatus);
    }
}
