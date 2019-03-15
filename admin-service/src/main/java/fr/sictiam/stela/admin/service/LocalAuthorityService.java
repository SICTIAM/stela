package fr.sictiam.stela.admin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.CertificateReports;
import fr.sictiam.signature.utils.CertUtils;
import fr.sictiam.stela.admin.dao.CertificateRepository;
import fr.sictiam.stela.admin.dao.LocalAuthorityRepository;
import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.Module;
import fr.sictiam.stela.admin.model.TokenResponse;
import fr.sictiam.stela.admin.model.UI.Views;
import fr.sictiam.stela.admin.model.event.LocalAuthorityEvent;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import fr.sictiam.stela.admin.service.util.OffsetBasedPageRequest;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.bouncycastle.asn1.x500.style.IETFUtils;
import org.bouncycastle.cert.jcajce.JcaX509CertificateHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.ZoneId;
import java.util.*;

@Service
public class LocalAuthorityService {

    private final LocalAuthorityRepository localAuthorityRepository;
    private final CertificateRepository certificateRepository;
    private final ProfileRepository profileRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalAuthorityService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${application.amqp.admin.createdKey}")
    private String createdKey;

    @Value("${application.amqp.admin.exchange}")
    private String exchange;

    @Value("${kernel.refreshToken}")
    private String refreshToken;

    @Value("${kernel.auth.token_endpoint}")
    private String tokenEndpoint;

    public LocalAuthorityService(LocalAuthorityRepository localAuthorityRepository,
            CertificateRepository certificateRepository, ProfileRepository profileRepository) {
        this.localAuthorityRepository = localAuthorityRepository;
        this.certificateRepository = certificateRepository;
        this.profileRepository = profileRepository;
    }

    public LocalAuthority createOrUpdate(LocalAuthority localAuthority) {
        localAuthority = localAuthorityRepository.saveAndFlush(localAuthority);

        LocalAuthorityEvent localAutorityCreation = new LocalAuthorityEvent(localAuthority);

        try {
            ObjectMapper mapper = new ObjectMapper();
            String body = mapper.writerWithView(Views.LocalAuthorityView.class)
                    .writeValueAsString(localAutorityCreation);
            MessageProperties messageProperties = new MessageProperties();
            messageProperties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
            Message amMessage = new Message(body.getBytes(), messageProperties);
            amqpTemplate.send(exchange, "", amMessage);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }

        return localAuthority;
    }

    public LocalAuthority modify(LocalAuthority localAuthority) {
        return createOrUpdate(localAuthority);
    }

    public void addModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.getOne(uuid);
        localAuthority.addModule(module);
        createOrUpdate(localAuthority);
    }

    public void removeModule(String uuid, Module module) {
        LocalAuthority localAuthority = localAuthorityRepository.getOne(uuid);
        localAuthority.removeModule(module);
        createOrUpdate(localAuthority);
    }

    public List<LocalAuthority> getAll() {
        return localAuthorityRepository.findAll();
    }

    public List<LocalAuthority> getAllWithPagination(Integer limit, Integer offset, String column,
            Sort.Direction direction) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, new Sort(direction, column));
        Page page = localAuthorityRepository.findAll(pageable);
        return page.getContent();
    }

    public List<LocalAuthority> getMineWithPagination(String agentUuid, Integer limit, Integer offset, String column,
            Sort.Direction direction) {
        Pageable pageable = new OffsetBasedPageRequest(offset, limit, new Sort(direction, column));
        Page page = localAuthorityRepository.findAllByProfiles_AgentUuidAndProfiles_AdminTrue(agentUuid, pageable);
        return page.getContent();
    }

    public Long countAll() {
        return localAuthorityRepository.countAll();
    }

    public Long countMine(String agentUuid) {
        return localAuthorityRepository.countMine(agentUuid);
    }

    public LocalAuthority getByUuid(String uuid) {
        return localAuthorityRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.local_authority_not_found"));
    }

    public Optional<LocalAuthority> getBySlugName(String slugName) {
        return localAuthorityRepository.findBySlugName(slugName);
    }

    public Optional<LocalAuthority> findByName(String name) {
        return localAuthorityRepository.findByName(name);
    }

    public Optional<LocalAuthority> findBySiren(String siren) {
        return localAuthorityRepository.findBySiren(siren);
    }

    public Optional<LocalAuthority> getByInstanceId(String instanceId) {
        return localAuthorityRepository.findByOzwilloInstanceInfo_InstanceId(instanceId);
    }

    public Certificate getCertificate(String uuid) {
        return certificateRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("notifications.admin.local_authority_certificate_not_found"));
    }

    public void addCertificate(String uuid, MultipartFile file)
            throws IOException, IllegalArgumentException, CertificateException {
        CertificateReports report = CertUtils.validateCertificate(file.getBytes());
        Indication indication = CertUtils.getCertificateValidationResult(report);
        LOGGER.info("DSS validation response : {}", indication);
        if (Indication.TOTAL_PASSED.equals(indication) || Indication.PASSED.equals(indication)) {
            Certificate certificate = buildCertificate(file);
            certificate = certificateRepository.save(certificate);
            LocalAuthority localAuthority = getByUuid(uuid);
            localAuthority.getCertificates().add(certificate);
            localAuthorityRepository.save(localAuthority);
        } else throw new IllegalArgumentException();
    }

    private Certificate buildCertificate(MultipartFile file) {
        try {
            X509Certificate cert = CertUtils.getCertificateFromBytes(file.getBytes());
            return new Certificate(
                    cert.getSerialNumber().toString(),
                    cert.getIssuerDN().getName(),
                    getSubjectSpecificCertInfo(cert, BCStyle.CN),
                    getSubjectSpecificCertInfo(cert, BCStyle.O),
                    getSubjectSpecificCertInfo(cert, BCStyle.OU),
                    getSubjectSpecificCertInfo(cert, BCStyle.E),
                    getIssuerSpecificCertInfo(cert, BCStyle.CN),
                    getIssuerSpecificCertInfo(cert, BCStyle.O),
                    getSubjectSpecificCertInfo(cert, BCStyle.E),
                    cert.getNotBefore().toInstant().atZone(ZoneId.systemDefault()).toLocalDate(),
                    cert.getNotAfter().toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            );
        } catch (CertificateException e) {
            LOGGER.error("Error while trying to retrieve certificate infos: {}", e.getMessage());
        } catch (IOException e) {
            LOGGER.error("Error while trying to read bytes from certificate file: {}", e.getMessage());
        }
        return null;
    }

    private String getSubjectSpecificCertInfo(X509Certificate cert, ASN1ObjectIdentifier bcStyle)
            throws CertificateEncodingException {
        X500Name x500name = new JcaX509CertificateHolder(cert).getSubject();
        return getSpecificInfo(x500name, bcStyle);
    }

    private String getIssuerSpecificCertInfo(X509Certificate cert, ASN1ObjectIdentifier bcStyle)
            throws CertificateEncodingException {
        X500Name x500name = new JcaX509CertificateHolder(cert).getIssuer();
        return getSpecificInfo(x500name, bcStyle);
    }

    private String getSpecificInfo(X500Name x500name, ASN1ObjectIdentifier bcStyle) {
        RDN[] rdn = x500name.getRDNs(ASN1ObjectIdentifier.getInstance(bcStyle));
        if (rdn.length == 0) return null;
        return IETFUtils.valueToString(rdn[0].getFirst().getValue());
    }

    public void deleteCertificate(String uuid, String certificateUuid) {
        LocalAuthority localAuthority = getByUuid(uuid);
        Certificate certificate = certificateRepository.findByUuid(certificateUuid).get();
        localAuthority.getCertificates().remove(certificate);
        localAuthorityRepository.save(localAuthority);
        certificateRepository.delete(certificate);
    }

    public Optional<LocalAuthority> getByCertificate(String serial, String issuer) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<LocalAuthority> query = builder.createQuery(LocalAuthority.class);
        Root<LocalAuthority> localAuthorityRoot = query.from(LocalAuthority.class);
        Join<LocalAuthority, Certificate> localAuthorityCertificateJoin = localAuthorityRoot.join("certificates");

        query.select(builder.construct(LocalAuthority.class, localAuthorityRoot.get("uuid")));

        List<Predicate> predicates = new ArrayList<>();
        predicates.add(builder.and(builder.equal(localAuthorityCertificateJoin.get("serial"), serial)));
        predicates.add(builder.and(builder.equal(localAuthorityCertificateJoin.get("issuer"), issuer)));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        List<LocalAuthority> results = entityManager.createQuery(query).setMaxResults(1).getResultList();
        return (results == null || results.isEmpty()) ? Optional.empty() : Optional.of(results.get(0));
    }

    public boolean isAgentAdmin(String agentUuid, String localAuthorityUuid) {

        return profileRepository.isAdmin(agentUuid, localAuthorityUuid);
    }

    public Optional<TokenResponse> getAccessTokenFromKernel(String siren) {
        LocalAuthority localAuthority = localAuthorityRepository.findBySiren(siren)
                .orElseThrow(() -> new NotFoundException("notifications.admin.local_authority_not_found"));

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("grant_type", "refresh_token");
        form.add("refresh_token", this.refreshToken);

        HttpHeaders headers = new HttpHeaders();
        headers.add(
                HttpHeaders.AUTHORIZATION,
                this.buildKernelAuthorizationHeaderValue(
                        localAuthority.getOzwilloInstanceInfo().getClientId(),
                        localAuthority.getOzwilloInstanceInfo().getClientSecret()));

        LOGGER.debug("[getSyncAccessTokenFromKernel] Token endpoint: {}", tokenEndpoint);

        try {
            ResponseEntity<TokenResponse> response = restTemplate.exchange(tokenEndpoint,
                    HttpMethod.POST, new HttpEntity<>(form, headers), TokenResponse.class);

            return Optional.ofNullable(response.getBody());
        } catch (RestClientResponseException e) {
            LOGGER.error(
                    "[getSyncAccessTokenFromKernel] Error while trying to retrieve access token from kernel : HTTP status {}, {}",
                    e.getRawStatusCode(),
                    e.getResponseBodyAsString());
            return Optional.empty();
        }
    }

    private String buildKernelAuthorizationHeaderValue(String clientId, String clientSecret) {
         byte[] encodedAuth = Base64.getEncoder().encode(String.format(Locale.ROOT, "%s:%s", clientId, clientSecret).getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }
}
