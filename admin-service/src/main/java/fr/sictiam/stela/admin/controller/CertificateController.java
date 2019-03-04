package fr.sictiam.stela.admin.controller;

import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.CertificateStatus;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.service.AgentService;
import fr.sictiam.stela.admin.service.ProfileService;
import fr.sictiam.stela.admin.service.util.CertUtilService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin/certificate")
public class CertificateController {

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

    @GetMapping("/is-valid")
    public Boolean hasValidCertificate(
            @RequestAttribute(value = "STELA-Certificate", required = false) Certificate certificate,
            @RequestAttribute(value = "STELA-Current-Profile-Paired-Certificate", required = false) Certificate pairedCertificate) {
        return certUtilService.checkCert(certificate, pairedCertificate);
    }

    @GetMapping("/verified-status")
    public CertificateStatus getVerifiedStatus(
            @RequestAttribute(value = "STELA-Certificate", required = false) Certificate certificate,
            @RequestAttribute(value = "STELA-Current-Profile-Paired-Certificate", required = false) Certificate pairedCertificate) {
        return certUtilService.getVerifiedStatus(certificate, pairedCertificate);
    }
}
