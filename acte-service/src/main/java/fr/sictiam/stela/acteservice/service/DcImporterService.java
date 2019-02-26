package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import javax.validation.constraints.NotNull;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class DcImporterService  implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(DcImporterService.class);

    private ExternalRestService externalRestService;

    private LocalAuthorityService localAuthorityService;

    private RestTemplate restTemplate;

    private ActeService acteService;

    @Value("${application.datacore.dataOwnerSiren}")
    private String dataOwnerSiren;

    @Value("${application.datacore.dcImporterUrl}")
    private String dcImporterUrl;

    @Value("${application.datacore.baseUri}")
    private String baseUri;

    @Value("${application.url}")
    private String applicationUrl;

    @Autowired
    public DcImporterService(ExternalRestService externalRestService, LocalAuthorityService localAuthorityService, RestTemplate restTemplate, ActeService acteService) {
        this.externalRestService = externalRestService;
        this.localAuthorityService = localAuthorityService;
        this.restTemplate = restTemplate;
        this.acteService = acteService;
    }

    public ResponseEntity<?> newResource(String type, String project, Map<String, Object> properties, String accessToken) throws RestClientResponseException {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, ContentType.APPLICATION_JSON.getMimeType());
        headers.add(HttpHeaders.AUTHORIZATION, "Bearer ".concat(accessToken));
        headers.add("X-Datacore-Project", project);

        return restTemplate.postForEntity(dcImporterUrl.concat("/dc/type/{type}"), new HttpEntity<>(properties, headers), String.class, type);
    }

    public Optional<String> getAccessTokenForDataOwner() {
        Optional<LocalAuthority> localAuthority = localAuthorityService.getBySiren(dataOwnerSiren);

        if(localAuthority.isPresent())
            return externalRestService.getAccessTokenFromKernel(localAuthority.get());
        else {
            LOGGER.warn("[getAccessTokenForDataOwner] Not found local authority for configured dataOwnerSiren {}", dataOwnerSiren);
            return Optional.empty();
        }
    }

    public void sendPublicActeToDcImporter(Acte acte) {
        Map<String, Object> resource = new HashMap<>();

        String formatActeDateCreation = acte.getCreation().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        resource.put("@id", this.buildActeDcUri(acte.getUuid(), acte.getLocalAuthority().getSiren(), formatActeDateCreation, acte.getCode()));
        resource.put("deliberation:collectivite", this.getLocalAuthorityDcId(acte.getLocalAuthority()));
        resource.put("deliberation:delib_id", acte.getUuid());
        resource.put("deliberation:delib_date", acte.getCreation().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        resource.put("deliberation:delib_matiere_code", this.getLevelTwoOfDeliberationMatiereCode(acte.getCode()));
        resource.put("deliberation:delib_matiere_nom", acte.getCodeLabel());
        resource.put("deliberation:delib_objet", acte.getObjet());
        resource.put("deliberation:delib_url", this.buildAttachementUrl(acte.getUuid()));

        Optional<String> kernelAccessToken = this.getAccessTokenForDataOwner();

        if(kernelAccessToken.isPresent()) {
            try {
                this.newResource("legal:deliberation_0", "legal_0", resource, kernelAccessToken.get());
            } catch (RestClientResponseException e) {
                LOGGER.error(
                        "[sendPublicActeToDcImporter] An error occured when sent public acte {} of collectivite {} to dc-importer : Status {} Body {}",
                        acte.getUuid(),
                        acte.getLocalAuthority().getSiren(),
                        e.getRawStatusCode(),
                        e.getResponseBodyAsString());
            }
        } else {
            LOGGER.warn(
                    "[sendPublicActeToDcImporter] Can't sent public acte {} of collectivite {} to dc-importer without kernel access token",
                    acte.getUuid(),
                    acte.getLocalAuthority().getSiren());
        }
    }

    private String buildActeDcUri(String uuid, String siret, String date, String codeMatiere) {
        return String.join("/", baseUri, "legal:deliberation_0", "FR", siret, uuid, date, codeMatiere);
    }

    private String buildCollectiviteDcUri(String siret) {
        return String.join("/", baseUri, "orgfr:Organisation_0", "FR", siret);
    }

    private String buildAttachementUrl(String uuid) {
        return String.join("/", applicationUrl, "api/acte/public", uuid, "file?disposition=attachment");
    }

    private String[] deliberationMatiereCodeAdapter(String acteCode) {
        return acteCode.replaceFirst("-", ".").split("-");
    }

    private String getLevelTwoOfDeliberationMatiereCode(String acteCode) {
        return this.deliberationMatiereCodeAdapter(acteCode)[0];
    }

    private String getLocalAuthorityDcId(LocalAuthority localAuthority) {
        Optional<String> dcId = externalRestService.getLocalAuthorityDcId(localAuthority);

        return dcId.orElseGet(() -> this.buildCollectiviteDcUri(localAuthority.getSiren()));
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        ActeHistory history = event.getActeHistory();
        Acte acte = acteService.getByUuid(history.getActeUuid());

        if(history.getStatus() == StatusType.ACK_RECEIVED && acte.isPublic()) {
            this.sendPublicActeToDcImporter(acte);
        }
    }
}
