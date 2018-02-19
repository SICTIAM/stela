package fr.sictiam.stela.pesservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.dao.SesileConfigurationRepository;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.SesileConfiguration;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurRequest;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurType;
import fr.sictiam.stela.pesservice.model.sesile.Document;
import fr.sictiam.stela.pesservice.model.sesile.ServiceOrganisation;
import fr.sictiam.stela.pesservice.service.exceptions.ProfileNotConfiguredForSesileException;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SesileService implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SesileService.class);

    private final PesAllerService pesService;

    private RestTemplate restTemplate = new RestTemplate();

    @Value("${application.sesile.apiUrl}")
    String sesileUrl;

    private final ExternalRestService externalRestService;

    private final SesileConfigurationRepository sesileConfigurationRepository;

    public SesileService(PesAllerService pesService, ExternalRestService externalRestService,
            SesileConfigurationRepository sesileConfigurationRepository) {
        this.pesService = pesService;
        this.externalRestService = externalRestService;
        this.sesileConfigurationRepository = sesileConfigurationRepository;
    }

    public SesileConfiguration createOrUpdate(SesileConfiguration sesileConfiguration) {
        return sesileConfigurationRepository.save(sesileConfiguration);
    }

    public SesileConfiguration getConfigurationByUuid(String uuid) {
        return sesileConfigurationRepository.findById(uuid).orElseGet(SesileConfiguration::new);
    }

    public void submitToSignature(PesAller pes) {

        try {
            SesileConfiguration sesileConfiguration = sesileConfigurationRepository.findById(pes.getProfileUuid())
                    .orElseThrow(ProfileNotConfiguredForSesileException::new);
            JsonNode profile = externalRestService.getProfile(pes.getProfileUuid());

            LocalDate localDate = LocalDate.now().plusDays(sesileConfiguration.getDaysToValidated());
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            ResponseEntity<Classeur> classeur = postClasseur(sesileConfiguration,
                    new ClasseurRequest(pes.getObjet(), "", localDate.format(dateTimeFormatter),
                            sesileConfiguration.getType(), sesileConfiguration.getServiceOrganisationNumber(),
                            sesileConfiguration.getVisibility(), profile.get("agent").get("email").asText()));

            Document document = addFileToclasseur(sesileConfiguration, pes.getAttachment().getFile(),
                    pes.getAttachment().getFilename(), classeur.getBody().getId()).getBody();

            pes.setSesileDocumentId(document.getId());
            pesService.save(pes);
            pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SIGNATURE);
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void checkPesSigned() {
        List<PesAller> pesAllers = pesService.getPendingSinature();
        pesAllers.forEach(pes -> {
            SesileConfiguration sesileConfiguration = sesileConfigurationRepository.findById(pes.getProfileUuid())
                    .orElseThrow(ProfileNotConfiguredForSesileException::new);
            if (pes.getSesileDocumentId() != null
                    && checkDocumentSigned(sesileConfiguration, pes.getSesileDocumentId())) {
                pes.getAttachment().setFile(getDocumentBody(sesileConfiguration, pes.getSesileDocumentId()));
                pes.setSigned(true);
                pesService.save(pes);
                pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SEND);
            }
        });
    }

    public ResponseEntity<Document> addFileToclasseur(SesileConfiguration sesileConfiguration, byte[] file,
            String fileName, int classeur) {

        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<String, Object>();

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
        headers.addAll(getHeaders(sesileConfiguration));
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        return restTemplate.exchange(sesileUrl + "/api/classeur/{classeur}/newDocuments", HttpMethod.POST,
                requestEntity, Document.class, classeur);
    }

    public MultiValueMap<String, String> getHeaders(SesileConfiguration sesileConfiguration) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        headers.add("token", sesileConfiguration.getToken());
        headers.add("secret", sesileConfiguration.getSecret());
        return headers;
    }

    public List<ServiceOrganisation> getServiceOrganisations(String profileUuid) throws IOException {
        SesileConfiguration sesileConfiguration = getConfigurationByUuid(profileUuid);
        if (StringUtils.isBlank(sesileConfiguration.getToken()))
            return new ArrayList<>();
        JsonNode profile = externalRestService.getProfile(profileUuid);
        String email = profile.get("agent").get("email").asText();
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
                getHeaders(sesileConfiguration));
        List<ServiceOrganisation> organisations = Arrays
                .asList(restTemplate.exchange(sesileUrl + "/api/user/services/{email}", HttpMethod.GET, requestEntity,
                        ServiceOrganisation[].class, email).getBody());
        List<ClasseurType> types = Arrays.asList(getTypes(sesileConfiguration).getBody());
        organisations.forEach(organisation -> {
            organisation.setTypes(types.stream().filter(type -> organisation.getType_classeur().contains(type.getId()))
                    .collect(Collectors.toList()));
        });
        return organisations;

    }

    public ResponseEntity<Classeur> checkClasseurStatus(SesileConfiguration sesileConfiguration, int classeur) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
                getHeaders(sesileConfiguration));
        return restTemplate.exchange(sesileUrl + "/api/classeur/{id}", HttpMethod.GET, requestEntity, Classeur.class,
                classeur);
    }

    public boolean checkDocumentSigned(SesileConfiguration sesileConfiguration, int document) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
                getHeaders(sesileConfiguration));
        return restTemplate
                .exchange(sesileUrl + "/api/document/{id}", HttpMethod.GET, requestEntity, Document.class, document)
                .getBody().isSigned();
    }

    public byte[] getDocumentBody(SesileConfiguration sesileConfiguration, int document) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
                getHeaders(sesileConfiguration));

        return restTemplate.exchange(sesileUrl + "/api/document/{id}/content", HttpMethod.GET, requestEntity,
                String.class, document).getBody().getBytes();
    }

    public ResponseEntity<Classeur> postClasseur(SesileConfiguration sesileConfiguration, ClasseurRequest classeur) {
        HttpEntity<ClasseurRequest> requestEntity = new HttpEntity<>(classeur, getHeaders(sesileConfiguration));
        return restTemplate.exchange(sesileUrl + "/api/classeur/", HttpMethod.POST, requestEntity, Classeur.class);
    }

    public ResponseEntity<ClasseurType[]> getTypes(SesileConfiguration sesileConfiguration) {
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(
                getHeaders(sesileConfiguration));
        return restTemplate.exchange(sesileUrl + "/api/classeur/types/", HttpMethod.GET, requestEntity,
                ClasseurType[].class);
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        if (StatusType.CREATED.equals(event.getPesHistory().getStatus())) {
            PesAller pes = pesService.getByUuid(event.getPesHistory().getPesUuid());
            // TODO remove
            boolean sendtest = true;
            if (sendtest || pes.isPj()) {
                pesService.updateStatus(pes.getUuid(), StatusType.PENDING_SEND);
            } else {
                submitToSignature(pes);
            }
        }
    }

}
