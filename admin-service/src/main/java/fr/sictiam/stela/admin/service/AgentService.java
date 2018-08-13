package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.AgentRepository;
import fr.sictiam.stela.admin.dao.CertificateRepository;
import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.Certificate;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.MigrationWrapper;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UserGatewayRequest;
import fr.sictiam.stela.admin.model.UserMigration;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Value("${application.usersgw.url}")
    private String usersgwUrl;

    @Value("${application.usersgw.user}")
    private String usersgwUser;

    @Value("${application.usersgw.password}")
    private String usersgwPassword;

    private final AgentRepository agentRepository;
    private final ProfileRepository profileRepository;
    private final CertificateRepository certificateRepository;
    private final LocalAuthorityService localAuthorityService;
    private final WorkGroupService workGroupService;
    private RestTemplate restTemplate = new RestTemplate();

    public AgentService(AgentRepository agentRepository, ProfileRepository profileRepository,
            CertificateRepository certificateRepository, LocalAuthorityService localAuthorityService,
            WorkGroupService workGroupService) {
        this.agentRepository = agentRepository;
        this.profileRepository = profileRepository;
        this.certificateRepository = certificateRepository;
        this.localAuthorityService = localAuthorityService;
        this.workGroupService = workGroupService;
    }

    private Agent createIfNotExists(Agent agent) {
        return agentRepository.findBySub(agent.getSub()).orElseGet(
                () -> agentRepository.findByEmail(agent.getEmail()).orElseGet(() -> agentRepository.save(agent)));
    }

    public Optional<Agent> findBySub(String sub) {
        return agentRepository.findBySub(sub);
    }

    public Optional<Agent> findByUuid(String uuid) {
        return agentRepository.findByUuid(uuid);
    }

    public Profile createAndAttach(Agent agent) {
        final String instanceId = agent.getInstanceId();
        LocalAuthority localAuthority = localAuthorityService.getByInstanceId(instanceId)
                .orElseThrow(() -> new NotFoundException("No local authority found for instance_id " + instanceId));
        Agent agentFetched = createIfNotExists(agent);
        agentFetched.setSub(agent.getSub());
        agentFetched.setAdmin(agent.isAdmin());
        agentFetched.setFamilyName(agent.getFamilyName());
        agentFetched.setGivenName(agent.getGivenName());
        agentFetched.setEmail(agent.getEmail());

        boolean hasProfileOnLocalAuthority = agentFetched.getProfiles().stream()
                .anyMatch(profile -> profile.getLocalAuthority().getUuid().equals(localAuthority.getUuid()));
        if (!hasProfileOnLocalAuthority) {
            Profile profile = new Profile(localAuthority, agentFetched, agentFetched.isAdmin());
            profileRepository.save(profile);
            agentFetched.getProfiles().add(profile);
            agentRepository.save(agentFetched);
            localAuthority.getProfiles().add(profile);
            localAuthorityService.createOrUpdate(localAuthority);
            return profile;
        } else {
            agentRepository.save(agentFetched);
            localAuthorityService.createOrUpdate(localAuthority);
            return agentFetched.getProfiles().stream()
                    .filter(profile -> profile.getLocalAuthority().getUuid().equals(localAuthority.getUuid()))
                    .findFirst().get();
        }
    }

    public void migrateUsers(MigrationWrapper migrationWrapper, String uuid) {
        WorkGroup workGroup = new WorkGroup(localAuthorityService.getByUuid(uuid),
                migrationWrapper.getModuleName() + "-migration");
        workGroup.setRights(migrationWrapper.getRights());
        workGroup = workGroupService.create(workGroup);
        LocalAuthority localAuthority = localAuthorityService.getByUuid(uuid);

        for (UserMigration userMigration : migrationWrapper.getUserMigrations()) {
            Optional<Agent> agentOpt = agentRepository.findByEmail(userMigration.getEmail());
            Agent agent;
            if (agentOpt.isPresent())
                agent = agentOpt.get();
            else {
                agent = new Agent(userMigration.getEmail());
                agent.setAdmin(false);
            }
            agent.setImported(true);

            Profile profile = null;
            if (agent.getUuid() != null) {
                Optional<Profile> profileOpt = profileRepository
                        .findByLocalAuthority_UuidAndAgent_Uuid(localAuthority.getUuid(), agent.getUuid());
                if (profileOpt.isPresent())
                    profile = profileOpt.get();
            }
            if (profile == null) {
                profile = new Profile(localAuthority, agent, agent.isAdmin());
                agent.getProfiles().add(profile);
            }
            agentRepository.save(agent);
            profile = profileRepository.save(profile);

            // FIXME: profiles are not realy added to the group
            workGroup.getProfiles().add(profile);
            workGroup = workGroupService.update(workGroup);
            profile.getGroups().add(workGroup);
            profile = profileRepository.save(profile);

            localAuthority.getProfiles().add(profile);
            localAuthorityService.createOrUpdate(localAuthority);
        }

        HttpHeaders headers = createHeaders(usersgwUser, usersgwPassword);
        UserGatewayRequest userGatewayRequest = new UserGatewayRequest(migrationWrapper.getUserMigrations().stream()
                .map(userMigration -> userMigration.getEmail()).collect(Collectors.toList()),
                localAuthority.getOzwilloInstanceInfo());
        HttpEntity<UserGatewayRequest> requestEntity = new HttpEntity<>(userGatewayRequest, headers);
        restTemplate.exchange(usersgwUrl, HttpMethod.POST, requestEntity, String.class);

    }

    HttpHeaders createHeaders(String username, String password) {
        return new HttpHeaders() {
            {
                String auth = username + ":" + password;
                byte[] encodedAuth = Base64.getEncoder().encode(auth.getBytes());
                String authHeader = "Basic " + new String(encodedAuth);
                set("Authorization", authHeader);
            }
        };
    }

    public List<Agent> getAllWithPagination(String search, String localAuthorityUuid, Integer limit, Integer offset,
            String column, String direction) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Agent> query = builder.createQuery(Agent.class);
        Root<Agent> agentRoot = query.from(Agent.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "familyName" : column;
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(search)) {
            predicates.add(builder.or(
                    builder.like(builder.lower(agentRoot.get("familyName")), "%" + search.toLowerCase() + "%"),
                    builder.like(builder.lower(agentRoot.get("givenName")), "%" + search.toLowerCase() + "%"),
                    builder.like(builder.lower(agentRoot.get("email")), "%" + search.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(localAuthorityUuid)) {
            Join<Agent, Profile> profileJoin = agentRoot.join("profiles");
            profileJoin.on(builder.equal(profileJoin.get("localAuthority").get("uuid"), localAuthorityUuid));
        }
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(agentRoot.get(columnAttribute))
                        : builder.desc(agentRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    public Long countAll() {
        return agentRepository.countAll();
    }

    public boolean isCertificateTaken(HttpServletRequest request) {
        Certificate currentCertificate = getCertInfosFromHeaders(request);
        Optional<Certificate> certificateOpt = certificateRepository
                .findBySerialAndIssuer(currentCertificate.getSerial(), currentCertificate.getIssuer());
        return certificateOpt.isPresent();
    }

    public void pairCertificate(HttpServletRequest request, String agendUuid) {
        Agent agent = findByUuid(agendUuid).orElseThrow(NotFoundException::new);
        if (agent.getCertificate() != null) {
            Certificate certifiacte = certificateRepository.findByUuid(agent.getCertificate().getUuid()).get();
            agent.setCertificate(null);
            agent = agentRepository.save(agent);
            certificateRepository.delete(certifiacte);
        }
        Certificate currentCertificate = getCertInfosFromHeaders(request);
        agent.setCertificate(currentCertificate);
        agentRepository.save(agent);
    }

    public Certificate getCertInfosFromHeaders(HttpServletRequest request) {
        return new Certificate(
                request.getHeader("x-ssl-client-m-serial"),
                request.getHeader("x-ssl-client-issuer-dn"),
                request.getHeader("x-ssl-client-s-dn-cn"),
                request.getHeader("x-ssl-client-s-dn-o"),
                request.getHeader("x-ssl-client-s-dn-ou"),
                request.getHeader("x-ssl-client-s-dn-email"),
                request.getHeader("x-ssl-client-i-dn-cn"),
                request.getHeader("x-ssl-client-i-dn-o"),
                request.getHeader("x-ssl-client-i-dn-email"),
                haDateToLocalDate(request.getHeader("x-ssl-client-not-before")),
                haDateToLocalDate(request.getHeader("x-ssl-client-not-after"))
        );
    }

    private LocalDate haDateToLocalDate(String timestampZ) {
        if (org.springframework.util.StringUtils.isEmpty(timestampZ)) return null;
        return LocalDateTime
                .parse(timestampZ.replace("Z", ""), DateTimeFormatter.ofPattern("yyMMddHHmmss"))
                .toLocalDate();
    }
}
