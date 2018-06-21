package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.CertificateStatus;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.service.AgentService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.util.CertUtilService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/certificate")
public class CertificateController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CertificateController.class);

    @Value("${application.certVerificationEnabled}")
    boolean certVerificationEnabled;

    private final ProfileService profileService;
    private final AgentService agentService;
    private final CertUtilService certUtilService;

    public CertificateController(ProfileService profileService, AgentService agentService,
            CertUtilService certUtilService) {
        this.profileService = profileService;
        this.agentService = agentService;
        this.certUtilService = certUtilService;
    }

    @PostMapping
    public ResponseEntity pairCertificate(HttpServletRequest request,
            @RequestAttribute("STELA-Current-Profile-UUID") String profileUuid) {
        if (!"VALID".equals(request.getHeader("x-ssl-status"))) {
            return new ResponseEntity(HttpStatus.PRECONDITION_FAILED);
        }
        if (agentService.isCertificateTaken(request)) {
            return new ResponseEntity(HttpStatus.CONFLICT);
        }
        Profile profile = profileService.getByUuid(profileUuid);
        agentService.pairCertificate(request, profile.getAgent().getUuid());
        return new ResponseEntity(HttpStatus.OK);
    }

    @GetMapping(value = "/is-valid")
    public Boolean hasValidCertificate(
            @RequestAttribute("STELA-Certificate") Certificate certificate,
            @RequestAttribute("STELA-Current-Profile-Paired-Certificate") Certificate pairedCertificate) {
        LOGGER.debug("certificate: {}", certificate.toString());
        LOGGER.debug("pairedCertificate: {}", pairedCertificate.toString());
        return !certVerificationEnabled || certUtilService.checkCert(certificate, pairedCertificate);
    }

    @GetMapping(value = "/verified-status")
    public CertificateStatus getVerifiedStatus(
            @RequestAttribute("STELA-Certificate") Certificate certificate,
            @RequestAttribute("STELA-Current-Profile-Paired-Certificate") Certificate pairedCertificate) {
        return certUtilService.getVerifiedStatus(certificate, pairedCertificate);
    }
}
