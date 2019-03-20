package fr.sictiam.stela.pesservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import eu.europa.esig.dss.validation.policy.rules.Indication;
import eu.europa.esig.dss.validation.reports.DetailedReport;
import fr.sictiam.signature.pes.producer.SigningPolicies.SigningPolicy1;
import fr.sictiam.signature.pes.verifier.CertificateProcessor.CertificatInformation1;
import fr.sictiam.signature.pes.verifier.*;
import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1;
import fr.sictiam.signature.pes.verifier.XadesInfoProcessor.XadesInfoProcessResult1;
import fr.sictiam.signature.utils.DateUtils;
import fr.sictiam.signature.utils.XadesUtils;
import fr.sictiam.stela.pesservice.dao.GenericDocumentRepository;
import fr.sictiam.stela.pesservice.dao.SesileConfigurationRepository;
import fr.sictiam.stela.pesservice.model.*;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.model.sesile.*;
import fr.sictiam.stela.pesservice.service.exceptions.MissingSignatureException;
import fr.sictiam.stela.pesservice.service.exceptions.SignatureException;
import io.vavr.control.Either;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.*;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;

import javax.validation.constraints.NotNull;
import java.io.*;
import java.security.cert.CertificateException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SesileService implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SesileService.class);

    private final PesAllerService pesService;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${application.sesile.apiUrl}")
    String sesileUrl;

    @Value("${application.sesile.apiV4Url}")
    String sesileV4Url;

    @Value("${application.url}")
    private String applicationUrl;

    private final ExternalRestService externalRestService;

    private final SesileConfigurationRepository sesileConfigurationRepository;

    private final GenericDocumentRepository genericDocumentRepository;

    private final LocalesService localesService;

    private final StorageService storageService;

    private final SignatureService signatureService;

    public SesileService(PesAllerService pesService, ExternalRestService externalRestService,
            SesileConfigurationRepository sesileConfigurationRepository, LocalesService localesService,
            GenericDocumentRepository genericDocumentRepository, StorageService storageService, SignatureService signatureService) {
        this.pesService = pesService;
        this.externalRestService = externalRestService;
        this.sesileConfigurationRepository = sesileConfigurationRepository;
        this.localesService = localesService;
        this.genericDocumentRepository = genericDocumentRepository;
        this.storageService = storageService;
        this.signatureService = signatureService;
    }

    public SesileConfiguration createOrUpdate(SesileConfiguration sesileConfiguration) {
        return sesileConfigurationRepository.save(sesileConfiguration);
    }

    public SesileConfiguration getConfigurationByUuid(String uuid) {
        return sesileConfigurationRepository.findById(uuid).orElseGet(SesileConfiguration::new);
    }

    public void submitToSignature(PesAller pes) {
        LOGGER.info("Submitting PES {} to signature...", pes.getObjet());
        try {
            SesileConfiguration sesileConfiguration = sesileConfigurationRepository.findById(pes.getProfileUuid())
                    .orElse(sesileConfigurationRepository.findById(pes.getLocalAuthority().getGenericProfileUuid())
                            .get());

            JsonNode profile = externalRestService.getProfile(pes.getProfileUuid());

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            String returnUrl = getReturnUrl(pes);
            String deadline =
                    getSesileValidationDate(pes.getValidationLimit() == null ?
                                    null :
                                    pes.getValidationLimit().format(dateTimeFormatter),
                            sesileConfiguration.getProfileUuid());

            ResponseEntity<Classeur> classeur = postClasseur(pes.getLocalAuthority(),
                    new ClasseurRequest(pes.getObjet(), StringUtils.defaultString(pes.getComment()),
                            deadline, sesileConfiguration.getType(),
                            pes.getServiceOrganisationNumber() != null ? pes.getServiceOrganisationNumber()
                                    : sesileConfiguration.getServiceOrganisationNumber(),
                            sesileConfiguration.getVisibility(), profile.get("agent").get("email").asText()),
                    returnUrl);

            byte[] fileContent = storageService.getAttachmentContent(pes.getAttachment());
            Document document = addFileToclasseur(pes.getLocalAuthority(), fileContent,
                    pes.getAttachment().getFilename(), classeur.getBody().getId()).getBody();

            pes.setSesileClasseurId(classeur.getBody().getId());
            pes.setSesileDocumentId(document.getId());
            if(pes.getLocalAuthority().getSesileNewVersion())
                pes.setSesileClasseurUrl(classeur.getBody().getUrl());

            pesService.save(pes);
            pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SIGNATURE);
        } catch (RestClientResponseException e) {
            LOGGER.error("[submitToSignature] Failed to send classeur to Sesile: {} ({})", e.getMessage(), e.getResponseBodyAsString());
            pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_SENDING_ERROR);
        } catch (Exception e) {
            LOGGER.error("[submitToSignature] Error while trying to submit to signature: {}", e.getMessage());
            pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_SENDING_ERROR);
        }
    }

    public void checkPesWithdrawn() {
        LOGGER.info("[checkPesWithdrawn] Cheking for PES being withdrawn...");
        List<PesAller.Light> pesAllers = pesService.getPendingSinature();
        pesAllers.stream()
                .filter(pes -> pes.getSesileClasseurId() != null && !pes.isPj())
                .filter(pes -> pes.getPesHistories().stream()
                        .noneMatch(pesHistory -> StatusType.CLASSEUR_WITHDRAWN.equals(pesHistory.getStatus()) || StatusType.CLASSEUR_DELETED.equals(pesHistory.getStatus())))
                .filter(pes -> checkClasseurWithdrawn(pes.getLocalAuthority(), pes.getSesileClasseurId()))
                .forEach(pes -> pesService.updateStatus(pes.getUuid(), StatusType.CLASSEUR_WITHDRAWN));
    }

    public void checkPesSigned() {
        LOGGER.info("[checkPesSigned] Cheking for new PES signatures...");
        List<PesAller.Light> pesAllers = pesService.getPendingSinature();
        pesAllers.forEach(pes -> {
            if (pes.getLastHistoryStatus().equals(StatusType.PENDING_SIGNATURE) && pes.getPesHistories().stream()
                    .noneMatch(pesHistory -> StatusType.CLASSEUR_WITHDRAWN.equals(pesHistory.getStatus()) || StatusType.CLASSEUR_DELETED.equals(pesHistory.getStatus()))) {
                try {
                    LOGGER.debug("[checkPesSigned] Checking document {} status", pes.getSesileDocumentId());
                    if (pes.getSesileDocumentId() != null
                            && checkDocumentSigned(pes.getLocalAuthority(), pes.getSesileDocumentId())) {
                        if(pes.getLocalAuthority().getSesileNewVersion() && pes.getSesileClasseurUrl() != null)
                            addSignedStatus(
                                    pes.getSesileClasseurId(),
                                    pes.getLocalAuthority(),
                                    pes.getUuid(),
                                    pes.getSesileClasseurUrl());
                        else
                            pesService.updateStatus(
                                    pes.getUuid(),
                                    StatusType.CLASSEUR_SIGNED);
                        LOGGER.debug("[checkPesSigned] Document {} signed from Sesile", pes.getSesileDocumentId());
                        byte[] file = getDocumentBody(pes.getLocalAuthority(), pes.getSesileDocumentId());
                        if (file != null) {
                            LOGGER.debug("[checkPesSigned] Sending document {} to Stela validation process", pes.getSesileDocumentId());
                            pesService.updateStatusAndAttachment(pes.getUuid(), StatusType.SIGNATURE_VALIDATION, file);
                        } else {
                            LOGGER.warn("[checkPesSigned] Document content for pes {} is null", pes.getUuid());
                        }
                    }
                } catch (RestClientException | UnsupportedEncodingException e) {
                    LOGGER.error("[checkPesSigned] Error on PES {} : {}", pes.getUuid(), e.getMessage());
                }
            }
        });
    }

    public SimplePesInformation computeSimplePesInformation(byte[] file) {
        ByteArrayInputStream bais = new ByteArrayInputStream(file);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PesAllerAnalyser pesAllerAnalyser = new PesAllerAnalyser(bais, stream);

        pesAllerAnalyser.setDoSchemaValidation(true);
        try {
            pesAllerAnalyser.computeSimpleInformation();
        } catch (InvalidPesAllerFileException e) {
            LOGGER.error("[computeSimplePesInformation] {}", e.getMessage());
        }
        return pesAllerAnalyser.getSimplePesInformation();
    }

    public boolean isSigned(SimplePesInformation simplePesInformation) {
        return simplePesInformation.isSigned();
    }

    public SignatureValidation isValidSignature(SimplePesInformation simplePesInformation) {
        PesAllerAnalyser pesAllerAnalyser = new PesAllerAnalyser(simplePesInformation);
        try {
            pesAllerAnalyser.computeSignaturesVerificationResults();
        } catch (InvalidPesAllerFileException e) {
            LOGGER.error("[isValidSignature] {}", e.getMessage());
        }
        pesAllerAnalyser.computeSignaturesTypeVerification();

        SignatureValidation signatureValidation = new SignatureValidation();
        List<SignatureValidationError> signatureValidationErrors = new ArrayList<>();
        signatureValidation.setSignatureValidationErrors(signatureValidationErrors);
        signatureValidation.setValid(true);
        if ((pesAllerAnalyser.isDoSchemaValidation()) && (!pesAllerAnalyser.isSchemaOK())) {
            signatureValidation.setValid(false);
            signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.INVALID_SCHEMA);
        }
        if (!signatureValidation.isValid()) {
            return signatureValidation;
        }

        for (Element element : simplePesInformation.getSignatureElements()) {
            SignatureVerifierResult verificationResult = pesAllerAnalyser.getSignaturesVerificationResults()
                    .get(element);
            if ((!verificationResult.isSignatureGlobalePresente())
                    && (verificationResult.getListeBordereauxNonSignes() != null)) {
                signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.NOT_SIGNED_CONTENT);
            }
            if (verificationResult.getUnverifiableSignatureException() != null) {
                signatureValidationErrors.add(SignatureValidationError.UNVERIFIABLE_SIGNATURE);
            } else {

                XadesInfoProcessResult1 xadesInfoProcessResult = verificationResult.getXadesInfoProcessResult();
                List<XMLDsigReference1> listRef = verificationResult.getSignatureAndRefsVerificationResult()
                        .getReferencesInfo();

                boolean isSomeSignedPropertyReference = false;
                for (XMLDsigReference1 ref : listRef) {
                    if (ref.isSignedPropertiesReferenceLookup(simplePesInformation.getPesDocument())) {
                        isSomeSignedPropertyReference = true;
                    }
                }

                boolean signatureVerifiedOk = verificationResult.getSignatureAndRefsVerificationResult()
                        .isSignatureVerified();

                boolean certificatProcessOk = verificationResult.getCertificateProcessException() == null;
                boolean certificatHashOk = (xadesInfoProcessResult.getSigCertExpectedHash() == null)
                        || (xadesInfoProcessResult.getSigCertExpectedHash()
                        .equals(xadesInfoProcessResult.getSigCertcalculatedHash()));

                boolean mainC14Ok = verificationResult.getSignatureAndRefsVerificationResult().isMainC14Accepted();
                boolean allrefsC14Ok = verificationResult.getSignatureAndRefsVerificationResult()
                        .isAllrefsC14Accepted();

                boolean certificateConfianceOk = false;
                CertificatInformation1 certificatInformation = null;
                if (certificatProcessOk) {
                    certificatInformation = verificationResult.getCertificatInformation();
                    certificateConfianceOk = (certificatInformation.getValidatedCertPath() != null)
                            && (!certificatInformation.getValidatedCertPath().isEmpty());
                }

                boolean certificatSerialNumberOk = (xadesInfoProcessResult.getSigCertExpectedSerialNumber() == null)
                        || (xadesInfoProcessResult.getSigCertExpectedSerialNumber()
                        .equals(xadesInfoProcessResult.getSigCertSerialNumber()));
                boolean certificateIssuerOk = (xadesInfoProcessResult.getSigCertExpectedIssuerName() == null)
                        || (xadesInfoProcessResult.getSigCertExpectedIssuerName().replaceAll(" ", "")
                        .equals(xadesInfoProcessResult.getSigCertIssuerName().replaceAll(" ", "")));
                boolean certificatdigitalSignatureOk = certificatInformation.getSigningCertificate()
                        .getKeyUsage() != null ? certificatInformation.getSigningCertificate().getKeyUsage()[1] : false;
                boolean certificatChainOk = certificatInformation.isSignCertAuthorized();
                boolean certificatChainAutoriseOk = certificatInformation.isAuthorizedCertPath();

                if (!certificateIssuerOk) {
                    certificateIssuerOk = (xadesInfoProcessResult.getSigCertExpectedIssuerName() == null)
                            || (xadesInfoProcessResult.getSigCertExpectedIssuerName().replaceAll(" ", "")
                            .equals(xadesInfoProcessResult.getSigCertIssuerNameRFC2253().replaceAll(" ", "")));
                }

                boolean certificatBasicConstraintsCritical = certificatInformation.isBasicConstraintCritical();

                boolean xadesProcessOk = verificationResult.getXadesExtractionException() == null;
                boolean xadesSigPolicyHashOk = (xadesInfoProcessResult.getSigExpectedSecurityPolicyIdHash() == null)
                        || (xadesInfoProcessResult.getSigSecurityPolicyIdHash() == null)
                        || (xadesInfoProcessResult.getSigExpectedSecurityPolicyIdHash()
                        .equals(xadesInfoProcessResult.getSigSecurityPolicyIdHash()));
                boolean problemRef = false;
                boolean problemSignedPropertyRef = false;
                for (XMLDsigReference1 ref : listRef) {
                    if (!ref.isVerified()) {
                        problemRef = true;
                    }

                    if (ref.isSignedPropertiesReferenceLookup(simplePesInformation.getPesDocument())) {
                        isSomeSignedPropertyReference = true;
                        if (!ref.isVerified()) {
                            problemSignedPropertyRef = true;
                        }
                    }
                }

                if (!((signatureVerifiedOk) && (isSomeSignedPropertyReference) && (certificatProcessOk)
                        && (certificatSerialNumberOk) && (certificateIssuerOk) && (certificateConfianceOk)
                        && ((certificatdigitalSignatureOk) || (!certificatBasicConstraintsCritical))
                        && (certificatHashOk) && (certificatChainOk) && (certificatChainAutoriseOk) && (mainC14Ok)
                        && (allrefsC14Ok))) {
                    signatureValidation.setValid(false);
                    signatureValidation.getSignatureValidationErrors()
                            .add(SignatureValidationError.SIGNATURE_CONTROL_ERRORS);
                }

                if (certificatProcessOk) {
                    if ((certificateConfianceOk)
                            && ((certificatdigitalSignatureOk) || (!certificatBasicConstraintsCritical))
                            && (certificatChainOk) && (certificatChainAutoriseOk)) {
                        if ((!certificatdigitalSignatureOk) && (!certificatBasicConstraintsCritical)) {
                            signatureValidation.getSignatureValidationErrors()
                                    .add(SignatureValidationError.WRONG_CERTIFICATE);
                        }
                    } else {
                        signatureValidation.getSignatureValidationErrors()
                                .add(SignatureValidationError.UNTRUSTED_CERTIFICATE);
                    }

                } else {
                    signatureValidation.getSignatureValidationErrors()
                            .add(SignatureValidationError.CERTIFICAT_RECOGNITION_ERROR);
                }

                if ((!mainC14Ok) || (!allrefsC14Ok)) {
                    signatureValidation.getSignatureValidationErrors()
                            .add(SignatureValidationError.RECOMMENDATION_NOT_RESPECTED);
                }

                if (xadesProcessOk) {
                    if (!isSomeSignedPropertyReference) {
                        signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.XADES_UNSIGNED);
                    }

                    if (problemSignedPropertyRef) {
                        signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.XADES_UPDATED);
                    }

                    if (!((xadesSigPolicyHashOk) && (certificatSerialNumberOk) && (certificateIssuerOk)
                            && (certificatHashOk))) {
                        signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.XADES_ERROR);
                    }

                    Date date = verificationResult.getXadesInfo().getSigningTime().getTime();
                    String tmp;
                    if (date != null) {
                        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
                        tmp = sdf.format(date);
                    } else {
                        tmp = null;
                    }

                    if (tmp != null) {
                        if (!DateUtils.isStrictUtcFormat(verificationResult.getXadesInfo().getSigningTimeAsString())) {
                            signatureValidation.getSignatureValidationErrors()
                                    .add(SignatureValidationError.DATE_FORMAT_ERROR);
                        }
                    } else {
                        signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.DATE_BLANK);
                    }

                    if (verificationResult.getXadesInfo().getSigClaimedRole() == null) {
                        signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.ROLE_BLANK);
                    }

                    String tmp1 = verificationResult.getXadesInfo().getSigPolicyId();

                    SigningPolicy1 signingPolicy = verificationResult.getXadesInfoProcessResult().getSigningPolicy();
                    if (signingPolicy != null) {

                        if ((tmp1 == null) || (tmp1.isEmpty())) {
                            signatureValidation.getSignatureValidationErrors()
                                    .add(SignatureValidationError.POLICY_ID_MISSING);
                        }

                        String hv = verificationResult.getXadesInfo().getSigPolicyHashDigestValue();
                        if (hv == null) {
                            signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.NO_POLICY);

                        }

                        String spq = verificationResult.getXadesInfo().getSigPolicyQualifier();
                        if (spq == null) {
                            signatureValidation.getSignatureValidationErrors()
                                    .add(SignatureValidationError.POLICY_QUALIFIER_MISSING);
                        }

                    }
                } else {
                    if (verificationResult.getXadesExtractionException() != null) {

                        signatureValidation.getSignatureValidationErrors()
                                .add(SignatureValidationError.XADES_EXCEPTION);

                    }
                    if (!isSomeSignedPropertyReference) {
                        signatureValidation.getSignatureValidationErrors().add(SignatureValidationError.XADES_UNSIGNED);
                    }
                }

            }
        }
        return signatureValidation;
    }

    public ResponseEntity<Document> addFileToclasseur(LocalAuthority localAuthority, byte[] file, String fileName,
            int classeur) throws RestClientResponseException {

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();

        map.add("name", fileName);
        map.add("filename", fileName);

        ByteArrayResource contentsAsResource = new ByteArrayResource(file) {
            @Override
            public String getFilename() {
                return fileName; // Filename has to be returned in order to be able to post.
            }
        };

        map.add("file", contentsAsResource);
        HttpHeaders headers = new HttpHeaders();
        headers.addAll(getHeaders(localAuthority));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        try {
            return restTemplate.exchange(getSesileUrl(localAuthority) + "/api/classeur/{classeur}/newDocuments", HttpMethod.POST,
                    requestEntity, Document.class, classeur);
        } catch (RestClientResponseException e) {
            LOGGER.error("[addFileToclasseur] Receiving a status code {} from SESILE url {} for organization {}:  {}",
                    e.getRawStatusCode(), getSesileUrl(localAuthority), localAuthority.getSiren(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public MultiValueMap<String, String> getHeaders(LocalAuthority localAuthority) {
        return getHeaders(localAuthority.getToken(), localAuthority.getSecret());
    }

    public MultiValueMap<String, String> getHeaders(String token, String secret) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("token", token);
        headers.add("secret", secret);
        return headers;
    }


    public HttpStatus verifyTokens(String token, String secret, boolean sesileNewVersion) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(token, secret));
        try {
            ResponseEntity<Object> response = restTemplate.exchange(getSesileUrl(sesileNewVersion) + "/api/user/",
                    HttpMethod.GET, requestEntity, Object.class);
            return response.getStatusCode();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.warn("[verifyTokens] Receiving a status code {} from SESILE url {}: {}",
                    e.getStatusCode(), getSesileUrl(sesileNewVersion), e.getMessage());
            return e.getStatusCode();
        }
    }

    public List<ServiceOrganisation> getServiceOrganisations(LocalAuthority localAuthority, String profileUuid)
            throws IOException {
        JsonNode profile = externalRestService.getProfile(profileUuid);
        String email = profile.get("agent").get("email").asText();
        //TODO: Refactor duplicated code
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        List<ServiceOrganisation> organisations;
        try {
            organisations = Arrays.asList(
                    localAuthority.getSesileNewVersion() ?
                            restTemplate.exchange(sesileV4Url + "/api/user/{userEmail}/org/{SIREN}/circuits", HttpMethod.GET,
                                    requestEntity, ServiceOrganisation[].class, email, localAuthority.getSiren()).getBody() :
                            restTemplate.exchange(sesileUrl + "/api/user/services/{email}", HttpMethod.GET,
                                    requestEntity, ServiceOrganisation[].class, email).getBody()
            );
        } catch (RestClientResponseException e) {
            LOGGER.warn("[getServiceOrganisations] Receiving a status code {} from SESILE: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return new ArrayList<>();
        }

        List<ClasseurType> types = getTypes(localAuthority);
        organisations.forEach(organisation -> {
            organisation.setTypes(types.stream().filter(type -> organisation.getType_classeur().contains(type.getId()))
                    .collect(Collectors.toList()));
        });
        return organisations;

    }

    public List<ServiceOrganisation> getServiceGenericOrganisations(LocalAuthority localAuthority, String email) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        try {
            return Arrays.asList(
                    localAuthority.getSesileNewVersion() ?
                            restTemplate.exchange(sesileV4Url + "/api/user/{userEmail}/org/{SIREN}/circuits", HttpMethod.GET,
                                    requestEntity, ServiceOrganisation[].class, email, localAuthority.getSiren()).getBody() :
                            restTemplate.exchange(sesileUrl + "/api/user/services/{email}", HttpMethod.GET, requestEntity,
                                    ServiceOrganisation[].class, email).getBody()
            );
        } catch (RestClientResponseException e) {
            LOGGER.warn("[getServiceGenericOrganisations] Receiving a status code {} from SESILE: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return new ArrayList<>();
        }
    }

    public List<ServiceOrganisation> getHeliosServiceOrganisations(LocalAuthority localAuthority, String email) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        List<ServiceOrganisation> organisations;
        try {
            organisations = Arrays.asList(
                    localAuthority.getSesileNewVersion() ?
                            restTemplate.exchange(sesileV4Url + "/api/user/{userEmail}/org/{SIREN}/circuits", HttpMethod.GET,
                                    requestEntity, ServiceOrganisation[].class, email, localAuthority.getSiren()).getBody() :
                            restTemplate.exchange(sesileUrl + "/api/user/services/{email}", HttpMethod.GET, requestEntity,
                                    ServiceOrganisation[].class, email).getBody()
            );
        } catch (RestClientResponseException e) {
            LOGGER.error("[getHeliosServiceOrganisations] Receiving a status code {} from SESILE: {}", e.getRawStatusCode(), e.getResponseBodyAsString());
            return new ArrayList<>();
        }

        List<Integer> types = getTypes(localAuthority).stream()
                .filter(type -> type.getNom().equals("Helios")).map(type -> type.getId()).collect(Collectors.toList());

        organisations = organisations.stream()
                .filter(orga -> orga.getType_classeur().stream().anyMatch(type -> types.contains(type)))
                .collect(Collectors.toList());
        return organisations;

    }

    public ResponseEntity<Classeur> checkClasseurStatus(LocalAuthority localAuthority, @NotNull Integer classeur) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        String url = getSesileUrl(localAuthority);
        String sesile3 = StringUtils.removeEnd(sesileUrl, "/");
        String sesile4 = StringUtils.removeEnd(sesileV4Url, "/");
        try {
            LOGGER.debug("[checkClasseurStatus] check classeur on {}", url + "/api/classeur/" + classeur);
            return restTemplate.exchange(url + "/api/classeur/{id}", HttpMethod.GET, requestEntity, Classeur.class,
                    classeur);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            // Quick fix : check on other Sesile version
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.info("[checkClasseurStatus] Receiving a status code {} from SESILE url {} for organization {} ({})", e.getStatusCode(),
                        url, localAuthority.getSiren(), e.getResponseBodyAsString());
                String fallbackUrl = url.equals(sesile4) ? sesile3 : sesile4;
                LOGGER.info("[checkClasseurStatus] Check classeur status on fallback url {}{}{}", fallbackUrl, "/api/classeur/", classeur);
                try {
                    return restTemplate.exchange(fallbackUrl + "/api/classeur/{id}",
                            HttpMethod.GET,
                            requestEntity,
                            Classeur.class,
                            classeur);
                } catch (HttpClientErrorException | HttpServerErrorException e2) {
                    LOGGER.error("[checkClasseurStatus] Receiving a status code {} from SESILE: {} ({}) on fallback url: {} for organization {}", e2.getStatusCode(),
                            e2.getMessage(), e2.getResponseBodyAsString(), fallbackUrl + "/api/classeur/" + classeur, localAuthority.getSiren());
                    return new ResponseEntity<>(e.getStatusCode());
                }
            } else {
                LOGGER.error("[checkClasseurStatus] Receiving a status code {} from SESILE url {} for the organization SIREN {} ({})", e.getStatusCode(),
                        url, localAuthority.getSiren(), e.getResponseBodyAsString());
            }
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    public Either<HttpStatus, Classeur> getClasseur(LocalAuthority localAuthority, @NotNull Integer classeur) {
        ResponseEntity<Classeur> classeurResponse = checkClasseurStatus(localAuthority, classeur);
        return classeurResponse.getStatusCode().isError() ?
            Either.left(classeurResponse.getStatusCode()) : Either.right(classeurResponse.getBody());
    }

    public boolean checkDocumentSigned(LocalAuthority localAuthority, int documentId) {
        Document document = getDocument(localAuthority, documentId);
        LOGGER.debug("[checkDocumentSigned] Document {} signed: {}", documentId, document != null && document.isSigned());
        return document != null && document.isSigned();
    }

    public boolean checkClasseurWithdrawn(LocalAuthority localAuthority, int classeurId) {
        ResponseEntity<Classeur> reponse = checkClasseurStatus(localAuthority, classeurId);
        if (reponse.getStatusCode().isError()) return false;
        return reponse.getBody().getStatus().equals(ClasseurStatus.WITHDRAWN);
    }

    public Document getDocument(LocalAuthority localAuthority, int documentId) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        String url = getSesileUrl(localAuthority);
        String sesile3 = StringUtils.removeEnd(sesileUrl, "/");
        String sesile4 = StringUtils.removeEnd(sesileV4Url, "/");
        ResponseEntity<Document> document = null;
        try {
            LOGGER.debug("[getDocument] check document on {}", url + "/api/document/" + documentId);
            document = restTemplate.exchange(url + "/api/document/{id}", HttpMethod.GET,
                    requestEntity, Document.class, documentId);
            return document.getStatusCode().isError() ? null : document.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            if (e.getStatusCode().is4xxClientError()) {
                LOGGER.info("[getDocument] Receiving a status code {} from SESILE url {} for organization {} ({})", e.getStatusCode(),
                        url, localAuthority.getSiren(), e.getMessage(), e.getResponseBodyAsString());
                String fallbackUrl = url.equals(sesile4) ? sesile3 : sesile4;
                LOGGER.info("[getDocument] Check document status on fallback url {}", fallbackUrl + "/api/document/" + documentId);
                try {
                    document = restTemplate.exchange(fallbackUrl + "/api/document/{id}",
                            HttpMethod.GET,
                            requestEntity,
                            Document.class,
                            documentId);
                    return document.getStatusCode().isError() ? null : document.getBody();
                } catch (HttpClientErrorException | HttpServerErrorException e2) {
                    LOGGER.error("[getDocument] Receiving a status code {} from SESILE: {} ({}) on fallback url : {}",
                            e2.getStatusCode(),
                            e2.getMessage(), e2.getResponseBodyAsString(), fallbackUrl + "/api/document/" + documentId);
                }
            } else {
                LOGGER.error("[getDocument] Receiving a status code {} from SESILE url {} for the organization SIREN {} ({})", e.getStatusCode(),
                        url, localAuthority.getSiren(), e.getResponseBodyAsString());
            }
            return null;
        }
    }

    public byte[] getDocumentBody(LocalAuthority localAuthority, int document)
            throws RestClientException, UnsupportedEncodingException {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        String url = getSesileUrl(localAuthority);
        try {
            return restTemplate.exchange( url + "/api/document/{id}/content", HttpMethod.GET, requestEntity,
                    String.class, document).getBody().getBytes("ISO-8859-1");
        } catch (RestClientResponseException e) {
            LOGGER.warn("[getDocumentBody] Receiving a status code {} from SESILE {} for document {}: {}",
                    e.getRawStatusCode(), url, document, e.getResponseBodyAsString());
            return null;
        } catch (NullPointerException e) {
            LOGGER.error("[getDocumentBody] Receiving no byte from SESILE {} for document {}:  {}", url, document, e.getMessage());
            return null;
        }
    }

    public ResponseEntity<Classeur> postClasseur(LocalAuthority localAuthority, ClasseurRequest classeur,
            String returnUrl)
            throws HttpClientErrorException {
        HttpEntity<ClasseurRequest> requestEntity = localAuthority.getSesileNewVersion() ?
                new HttpEntity<>(new ClasseurSirenRequest(classeur, localAuthority.getSiren(), returnUrl),
                        getHeaders(localAuthority)) :
                new HttpEntity<>(classeur, getHeaders(localAuthority));
        try {
            return restTemplate.exchange(getSesileUrl(localAuthority) + "/api/classeur/", HttpMethod.POST, requestEntity, Classeur.class);
        } catch (RestClientResponseException e) {
            LOGGER.error("[postClasseur] Receiving a status code {} from SESILE url {} for organization {} : {}",
                    e.getRawStatusCode(), getSesileUrl(localAuthority), localAuthority.getSiren(), e.getResponseBodyAsString());
            throw e;
        }
    }

    public List<ClasseurType> getTypes(LocalAuthority localAuthority) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        List<ClasseurType> types;
        try {
            types = Arrays.asList(restTemplate.exchange(getSesileUrl(localAuthority) + "/api/classeur/types/", HttpMethod.GET, requestEntity,
                    ClasseurType[].class).getBody());
        } catch (RestClientResponseException e) {
            LOGGER.warn("[getTypes] Receiving a status code {} from SESILE url {}: {}", e.getRawStatusCode(), getSesileUrl(localAuthority), e.getResponseBodyAsString());
            types = new ArrayList<>();
        }
        return types;
    }

    public Pair<StatusType, String> getSignatureStatus(byte[] file) {
        String errorMessage = "";
        StatusType status = StatusType.PENDING_SEND;
        SimplePesInformation simplePesInformation = computeSimplePesInformation(file);

        if (isSigned(simplePesInformation)) {
            SignatureValidation signatureValidation = isValidSignature(simplePesInformation);
            Indication indication;
            try {
                DetailedReport report = XadesUtils.validateXadesSignature(file);
                indication = XadesUtils.getCertificateValidationResult(report);
            } catch (IOException | CertificateException e) {
                LOGGER.error("Error while verifying the signature: {}", e.getMessage());
                indication = Indication.TOTAL_FAILED;
            }
            LOGGER.debug("Indication: {}", indication.toString());
            if (!signatureValidation.isValid()
                    || (!Indication.TOTAL_PASSED.equals(indication) && !Indication.PASSED.equals(indication))) {
                status = StatusType.SIGNATURE_INVALID;
                errorMessage = signatureValidation.getSignatureValidationErrors().stream()
                        .map(error -> localesService.getMessage("fr", "pes", "pes.signature_errors." + error.name()))
                        .collect(Collectors.joining("\n"));
            }
        } else {
            status = StatusType.SIGNATURE_MISSING;
        }
        return new ImmutablePair<>(status, errorMessage);
    }

    public boolean hasSignature(byte[] file) {
        SimplePesInformation simplePesInformation = computeSimplePesInformation(file);
        return simplePesInformation.isSigned();
    }

    public Optional<GenericDocument> getGenericDocument(Integer fluxId) {
        return genericDocumentRepository.findById(fluxId);
    }

    public GenericDocument saveGenericDocument(GenericDocument genericDocument) {
        return genericDocumentRepository.save(genericDocument);
    }

    public String getSesileValidationDate(String initialDate, String profileUuid) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        LocalDateTime date;
        if (StringUtils.isEmpty(initialDate)) {
            Optional<SesileConfiguration> sesileConfiguration = sesileConfigurationRepository.findById(profileUuid);
            date = LocalDateTime.now().plusDays(sesileConfiguration.isPresent() ?
                    sesileConfiguration.get().getDaysToValidated() :
                    3);
        } else {
            date = LocalDate.parse(initialDate, dateTimeFormatter).atStartOfDay();
        }

        if (date.isBefore(LocalDate.now().atStartOfDay())) {
            date = LocalDateTime.now().plusDays(3);
        }

        return date.format(dateTimeFormatter);
    }

    private String getSesileUrl(LocalAuthority localAuthority) {
        return getSesileUrl(localAuthority.getSesileNewVersion());
    }

    private String getSesileUrl(boolean sesileNewVersion) {
        return StringUtils.removeEnd(sesileNewVersion ? sesileV4Url : sesileUrl, "/");
    }

    public String getReturnUrl(PesAller pes) {
        String token = pesService.getToken(pes);
        return applicationUrl + "/api/pes/sesile/signature-hook/" + token + "/" + pes.getUuid();
    }

    public void updatePesStatus(PesAller pes, String status, MultipartFile file) throws IOException {
        if (status.equals("SIGNED")) {
            addSignedStatus(pes.getSesileClasseurId(), pes.getLocalAuthority(), pes.getUuid(), pes.getSesileClasseurUrl());
            pesService.updateStatusAndAttachment(pes.getUuid(), StatusType.SIGNATURE_VALIDATION, file.getBytes());
        } else {
            pesService.updateStatus(pes.getUuid(), StatusType.valueOf("CLASSEUR_" + status));
        }
    }

    public ResponseEntity<Classeur> getClasseur(int id, LocalAuthority localAuthority) {
        String sesileUrl = getSesileUrl(localAuthority);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));

        try {
            LOGGER.debug("[getClasseur] get classeur {} on {}", id, sesileUrl);
            return restTemplate.exchange(sesileUrl + "/api/classeur/{id}", HttpMethod.GET, requestEntity, Classeur.class,
                    id);
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            LOGGER.error("[getClasseur] Receiving a status code {} from SESILE: {} ({})", e.getStatusCode(),
                    e.getMessage(), e.getResponseBodyAsString());
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    private Optional<Action> getSignedActionOfClasseur(Classeur classeur) {
        return classeur.getActions().stream().filter(action ->
                (action.getAction().equals("Signature") && action.getCommentaire().equals("Classeur signé"))).findFirst();
    }

    private void addSignedStatus(Integer classeurId, LocalAuthority localAuthority, String pesUuid, String classeurUrl) {
        ResponseEntity<Classeur> responseClasseur = getClasseur(classeurId, localAuthority);
        if (responseClasseur.getStatusCode().is2xxSuccessful()) {
            Optional<Action> signedAction = getSignedActionOfClasseur(responseClasseur.getBody());

            if (signedAction.isPresent()) {
                Map<String, String> actionInfos = new HashMap<>();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm");
                actionInfos.put("userName", signedAction.get().getUsername());
                actionInfos.put("date", simpleDateFormat.format(signedAction.get().getDate()));
                String statusMessage = localesService.getMessage("fr", "pes", "$.pes.page.signed_by_on",
                        actionInfos);
                pesService.updateStatus(
                        pesUuid,
                        StatusType.CLASSEUR_SIGNED,
                        statusMessage);
            } else {
                pesService.updateStatus(
                        pesUuid,
                        StatusType.CLASSEUR_SIGNED);
                LOGGER.warn("[addSignedStatus] No signature action found for classeur {} of PES {}", classeurUrl, pesUuid);
            }
        }
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        if (StatusType.CREATED.equals(event.getPesHistory().getStatus())
                || StatusType.RECREATED.equals(event.getPesHistory().getStatus())) {
            PesAller pes = pesService.getByUuid(event.getPesHistory().getPesUuid());
            boolean sesileSubscription = pes.getLocalAuthority().getSesileSubscription() != null ?
                    pes.getLocalAuthority().getSesileSubscription() : false;

            if (pes.isPj() || pesService.isAPesOrmc(pes)) {
                pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SEND);
            } else if (!sesileSubscription || hasSignature(storageService.getAttachmentContent(pes.getAttachment()))) {
                pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_VALIDATION);
            } else {
                submitToSignature(pes);
            }
        } else if (StatusType.SIGNATURE_VALIDATION.equals(event.getPesHistory().getStatus())) {
            PesAller pes = pesService.getByUuid(event.getPesHistory().getPesUuid());
            try {
                signatureService.validatePes(storageService.getAttachmentContent(pes.getAttachment()));
                pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SEND);
            } catch (SignatureException e) {
                LOGGER.error("Error while validating signature: {}", e.getMessage());
                pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_INVALID, localesService.getMessage("fr",
                        "pes", "pes.signature_errors." + e.getMessage()));
            } catch (MissingSignatureException e) {
                pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_MISSING);
            }
        }
    }

}
