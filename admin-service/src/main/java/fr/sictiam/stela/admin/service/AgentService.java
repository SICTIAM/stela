package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.AgentRepository;
import fr.sictiam.stela.admin.dao.ProfileRepository;
import fr.sictiam.stela.admin.model.Agent;
import fr.sictiam.stela.admin.model.LocalAuthority;
import fr.sictiam.stela.admin.model.MigrationWrapper;
import fr.sictiam.stela.admin.model.Profile;
import fr.sictiam.stela.admin.model.UserMigration;
import fr.sictiam.stela.admin.model.WorkGroup;
import fr.sictiam.stela.admin.service.exceptions.NotFoundException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AgentService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final AgentRepository agentRepository;
    private final ProfileRepository profileRepository;
    private final LocalAuthorityService localAuthorityService;
    private final WorkGroupService workGroupService;

    public AgentService(AgentRepository agentRepository, ProfileRepository profileRepository,
            LocalAuthorityService localAuthorityService, WorkGroupService workGroupService) {
        this.agentRepository = agentRepository;
        this.profileRepository = profileRepository;
        this.localAuthorityService = localAuthorityService;
        this.workGroupService = workGroupService;
    }

    private Agent createIfNotExists(Agent agent) {
        return agentRepository.findBySub(agent.getSub()).orElseGet(() -> agentRepository.save(agent));
    }

    public Optional<Agent> findBySub(String sub) {
        return agentRepository.findBySub(sub);
    }

    public Optional<Agent> findByUuid(String uuid) {
        return agentRepository.findByUuid(uuid);
    }

    public Profile createAndAttach(Agent agent) {
        final String slugName = agent.getSlugName();
        LocalAuthority localAuthority = localAuthorityService.getBySlugName(slugName)
                .orElseThrow(() -> new NotFoundException("No local authority found for slug " + slugName));
        Agent agentFetched = createIfNotExists(agent);

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
            if (agentOpt.isPresent()) agent = agentOpt.get();
            else {
                agent = new Agent(userMigration.getEmail());
                agent.setAdmin(false);
            }
            agent.setImported(true);

            Profile profile = null;
            if (agent.getUuid() != null) {
                Optional<Profile> profileOpt = profileRepository.findByLocalAuthority_UuidAndAgent_Uuid(
                        localAuthority.getUuid(), agent.getUuid());
                if (profileOpt.isPresent()) profile = profileOpt.get();
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
}
