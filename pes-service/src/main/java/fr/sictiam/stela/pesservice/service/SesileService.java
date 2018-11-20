package fr.sictiam.stela.pesservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.andrewoma.dexx.collection.Pair;
import fr.sictiam.signature.pes.producer.SigningPolicies.SigningPolicy1;
import fr.sictiam.signature.pes.verifier.CertificateProcessor.CertificatInformation1;
import fr.sictiam.signature.pes.verifier.InvalidPesAllerFileException;
import fr.sictiam.signature.pes.verifier.PesAllerAnalyser;
import fr.sictiam.signature.pes.verifier.SignatureValidation;
import fr.sictiam.signature.pes.verifier.SignatureValidationError;
import fr.sictiam.signature.pes.verifier.SignatureVerifierResult;
import fr.sictiam.signature.pes.verifier.SimplePesInformation;
import fr.sictiam.signature.pes.verifier.XMLDsigSignatureAndReferencesProcessor.XMLDsigReference1;
import fr.sictiam.signature.pes.verifier.XadesInfoProcessor.XadesInfoProcessResult1;
import fr.sictiam.signature.utils.DateUtils;
import fr.sictiam.stela.pesservice.dao.GenericDocumentRepository;
import fr.sictiam.stela.pesservice.dao.SesileConfigurationRepository;
import fr.sictiam.stela.pesservice.model.GenericDocument;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.SesileConfiguration;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurRequest;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurSirenRequest;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurStatus;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurType;
import fr.sictiam.stela.pesservice.model.sesile.Document;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Element;

import javax.validation.constraints.NotNull;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

    public SesileService(PesAllerService pesService, ExternalRestService externalRestService,
            SesileConfigurationRepository sesileConfigurationRepository, LocalesService localesService,
            GenericDocumentRepository genericDocumentRepository, StorageService storageService) {
        this.pesService = pesService;
        this.externalRestService = externalRestService;
        this.sesileConfigurationRepository = sesileConfigurationRepository;
        this.localesService = localesService;
        this.genericDocumentRepository = genericDocumentRepository;
        this.storageService = storageService;
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
            pesService.save(pes);
            pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SIGNATURE);
        } catch (HttpClientErrorException e) {
            LOGGER.error("[submitToSignature] Failed to send classeur to Sesile: {} ({})", e.getMessage(), e.getResponseBodyAsString());
            pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_SENDING_ERROR);
        } catch (Exception e) {
            LOGGER.error("[submitToSignature] Error while trying to submit to signature: {}", e.getMessage());
            pesService.updateStatus(pes.getUuid(), StatusType.SIGNATURE_SENDING_ERROR);
        }
    }

    public void checkPesWithdrawn() {
        LOGGER.info("Cheking for PES being withdrawn...");
        List<PesAller.Light> pesAllers = pesService.getPendingSinature();
        pesAllers.forEach(pes -> {
            if (pes.getPesHistories().stream()
                    .noneMatch(pesHistory -> StatusType.CLASSEUR_WITHDRAWN.equals(pesHistory.getStatus()) || StatusType.CLASSEUR_DELETED.equals(pesHistory.getStatus()))) {
                if (pes.getSesileClasseurId() != null
                        && checkClasseurWithdrawn(pes.getLocalAuthority(), pes.getSesileClasseurId())) {
                    pesService.updateStatus(pes.getUuid(), StatusType.CLASSEUR_WITHDRAWN);
                }
            }
        });
    }

    public void checkPesSigned() {
        LOGGER.info("Cheking for new PES signatures...");
        List<PesAller.Light> pesAllers = pesService.getPendingSinature();
        pesAllers.forEach(pes -> {
            if (pes.getPesHistories().stream()
                    .noneMatch(pesHistory -> StatusType.CLASSEUR_WITHDRAWN.equals(pesHistory.getStatus()) || StatusType.CLASSEUR_DELETED.equals(pesHistory.getStatus()))) {
                try {
                    if (pes.getSesileDocumentId() != null
                            && checkDocumentSigned(pes.getLocalAuthority(), pes.getSesileDocumentId())) {
                        byte[] file = getDocumentBody(pes.getLocalAuthority(), pes.getSesileDocumentId());
                        if (file != null) {
                            Pair<StatusType, String> signatureResult = getSignatureStatus(file);
                            StatusType status = signatureResult.component1();
                            String errorMessage = signatureResult.component2();
                            // HACK: Prevent from incrementing SIGNATURE_MISSING when the PES is stuck on SESILE
                            if (!StatusType.SIGNATURE_MISSING.equals(pes.getLastHistoryStatus())
                                    || !StatusType.SIGNATURE_MISSING.equals(signatureResult.component1())) {
                                updatePesWithSignature(pes.getUuid(), file, status, errorMessage);
                            }
                        }
                    }
                } catch (RestClientException | UnsupportedEncodingException e) {
                    LOGGER.error("Error on PES {} : {}", pes.getUuid(), e.getMessage());
                }
            }
        });
    }

    private void updatePesWithSignature(String pesUuid, byte[] file, StatusType status, String errorMessage) {
        PesAller pesAller = pesService.getByUuid(pesUuid);
        pesAller.setSigned(status.equals(StatusType.PENDING_SEND));
        storageService.updateAttachment(pesAller.getAttachment(), file);
        pesService.save(pesAller);
        pesService.updateStatus(pesUuid, status, errorMessage);
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
            int classeur) throws HttpClientErrorException {

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
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
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


    public boolean verifyTokens(String token, String secret, boolean sesileNewVersion) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(token, secret));
        try {
            ResponseEntity<Object> response = restTemplate.exchange(getSesileUrl(sesileNewVersion) + "/api/user/",
                    HttpMethod.GET, requestEntity, Object.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
            return false;
        }
    }

    public List<ServiceOrganisation> getServiceOrganisations(LocalAuthority localAuthority, String profileUuid)
            throws IOException {
        JsonNode profile = externalRestService.getProfile(profileUuid);
        String email = profile.get("agent").get("email").asText();
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
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
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
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
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
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
            return new ArrayList<>();
        }

        List<Integer> types = getTypes(localAuthority).stream()
                .filter(type -> type.getNom().equals("Helios")).map(type -> type.getId()).collect(Collectors.toList());

        organisations = organisations.stream()
                .filter(orga -> orga.getType_classeur().stream().anyMatch(type -> types.contains(type)))
                .collect(Collectors.toList());
        return organisations;

    }

    public ResponseEntity<Classeur> checkClasseurStatus(LocalAuthority localAuthority, Integer classeur) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        String url = getSesileUrl(localAuthority);
        String sesile3 = StringUtils.removeEnd(sesileUrl, "/");
        String sesile4 = StringUtils.removeEnd(sesileV4Url, "/");
        try {
            LOGGER.debug("[checkClasseurStatus] check classeur on {}", url + "/api/classeur/" + classeur);
            return restTemplate.exchange(url + "/api/classeur/{id}", HttpMethod.GET, requestEntity, Classeur.class,
                    classeur);
        } catch (HttpClientErrorException e) {
            LOGGER.error("[checkClasseurStatus] Receiving a status code {} from SESILE: {} ({})", e.getStatusCode(),
                    e.getMessage(), e.getResponseBodyAsString());
            // Quick fix : check on other Sesile version
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                String fallbackUrl = url.equals(sesile4) ? sesile3 : sesile4;
                LOGGER.warn("[checkClasseurStatus] Check classeur status on fallback url {}", fallbackUrl + "/api" +
                        "/classeur/" + classeur);
                try {
                    return restTemplate.exchange(fallbackUrl + "/api/classeur/{id}",
                            HttpMethod.GET,
                            requestEntity,
                            Classeur.class,
                            classeur);
                } catch (HttpClientErrorException e2) {
                    LOGGER.error("[checkClasseurStatus] Receiving a status code {} from SESILE: {} ({}) on fallback " +
                                    "url: {}", e2.getStatusCode(),
                            e2.getMessage(), e2.getResponseBodyAsString(), fallbackUrl + "/api/classeur/" + classeur);
                    return new ResponseEntity<>(e.getStatusCode());
                }
            }
            return new ResponseEntity<>(e.getStatusCode());
        }
    }

    public boolean checkDocumentSigned(LocalAuthority localAuthority, int documentId) {
        Document document = getDocument(localAuthority, documentId);
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
        } catch (HttpClientErrorException e) {
            LOGGER.error("[getDocument] Receiving a status code {} from SESILE: {} ({})", e.getStatusCode(),
                    e.getMessage(), e.getResponseBodyAsString());
            if (e.getStatusCode() == HttpStatus.FORBIDDEN) {
                String fallbackUrl = url.equals(sesile4) ? sesile3 : sesile4;
                LOGGER.warn("[getDocument] Check document status on fallback url {}",
                        fallbackUrl + "/api/document/" + documentId);
                try {
                    document = restTemplate.exchange(fallbackUrl + "/api/document/{id}",
                            HttpMethod.GET,
                            requestEntity,
                            Document.class,
                            documentId);
                    return document.getStatusCode().isError() ? null : document.getBody();
                } catch (HttpClientErrorException e2) {
                    LOGGER.error("[getDocument] Receiving a status code {} from SESILE: {} ({}) on fallback url : {}",
                            e2.getStatusCode(),
                            e2.getMessage(), e2.getResponseBodyAsString(), fallbackUrl + "/api/document/" + documentId);
                }
            }
            return null;
        }
    }

    public byte[] getDocumentBody(LocalAuthority localAuthority, int document)
            throws RestClientException, UnsupportedEncodingException {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        try {
            return restTemplate.exchange(getSesileUrl(localAuthority) + "/api/document/{id}/content", HttpMethod.GET, requestEntity,
                    String.class, document).getBody().getBytes("ISO-8859-1");
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
            return null;
        } catch (NullPointerException e) {
            LOGGER.error("Receiving no byte from SESILE: {}", e.getMessage());
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
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
            throw e;
        }
    }

    public List<ClasseurType> getTypes(LocalAuthority localAuthority) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(getHeaders(localAuthority));
        List<ClasseurType> types;
        try {
            types = Arrays.asList(restTemplate.exchange(getSesileUrl(localAuthority) + "/api/classeur/types/", HttpMethod.GET, requestEntity,
                    ClasseurType[].class).getBody());
        } catch (HttpClientErrorException e) {
            LOGGER.error("Receiving a status code {} from SESILE: {}", e.getStatusCode(), e.getMessage());
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
            if (!signatureValidation.isValid()) {
                status = StatusType.SIGNATURE_INVALID;
                errorMessage = signatureValidation.getSignatureValidationErrors().stream()
                        .map(error -> localesService.getMessage("fr", "pes", "pes.signature_errors." + error.name()))
                        .collect(Collectors.joining("\n"));
            }
        } else {
            status = StatusType.SIGNATURE_MISSING;
        }
        return new Pair<StatusType, String>(status, errorMessage);

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
            pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SEND, file.getBytes(), file.getOriginalFilename());
        } else {
            pesService.updateStatus(pes.getUuid(), StatusType.valueOf("CLASSEUR_" + status));
        }
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        if (StatusType.CREATED.equals(event.getPesHistory().getStatus())
                || StatusType.RECREATED.equals(event.getPesHistory().getStatus())) {
            PesAller pes = pesService.getByUuid(event.getPesHistory().getPesUuid());
            boolean sesileSubscription = pes.getLocalAuthority().getSesileSubscription() != null ?
                    pes.getLocalAuthority().getSesileSubscription() : false;
            Pair<StatusType, String> signatureResult = getSignatureStatus(storageService.getAttachmentContent(pes.getAttachment()));
            pes.setSigned(signatureResult.component1().equals(StatusType.PENDING_SEND));
            pesService.save(pes);
            if (pes.isPj()) {
                pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SEND);
            } else if (!sesileSubscription || pes.isSigned()) {
                pesService.updateStatus(pes.getUuid(), signatureResult.component1(), signatureResult.component2());
            } else {
                submitToSignature(pes);
            }
        }
    }

}
