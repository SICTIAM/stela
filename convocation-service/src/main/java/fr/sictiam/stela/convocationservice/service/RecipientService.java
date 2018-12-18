package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.RecipientRepository;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.exception.RecipientExistsException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class RecipientService {

    private static Logger LOGGER = LoggerFactory.getLogger(RecipientService.class);

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    RecipientRepository recipientRepository;

    @Autowired
    ExternalRestService externalRestService;

    @Autowired
    LocalAuthorityService localAuthorityService;


    public Recipient createFrom(String firstname, String lastname, String email,
            String phoneNumber, String localAuthorityUuid) {

        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthorityUuid);

        Optional<Recipient> exist = recipientRepository.findByEmailAndLocalAuthorityUuid(email, localAuthorityUuid);
        if (exist.isPresent()) {
            LOGGER.error("A recipient with email {} already exists in local authority {}", email, localAuthority.getName());
            throw new RecipientExistsException();
        }

        Recipient recipient = new Recipient(firstname, lastname, email, phoneNumber, localAuthority);
        recipient.setToken(generateToken(recipient));
        recipient.setAssemblyTypes(new HashSet<>());
        return recipient;
    }

    public Recipient getRecipient(String uuid) {

        return recipientRepository.findByUuid(uuid).orElseThrow(NotFoundException::new);
    }

    public Recipient save(Recipient recipient) {

        return recipientRepository.saveAndFlush(recipient);
    }

    public void setActive(String uuid, boolean active) {
        Recipient recipient = getRecipient(uuid);
        recipient.setActive(active);
        recipient.setInactivityDate(active ? null : LocalDateTime.now());
        save(recipient);
    }

    public Long countAllWithQuery(String multifield, String firstname, String lastname, String email,
            Boolean active, Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<Recipient> recipientRoot = query.from(Recipient.class);

        List<Predicate> predicates = getQueryPredicates(builder, recipientRoot, multifield, firstname, lastname,
                email, active, currentLocalAuthUuid);
        query.select(builder.count(recipientRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<Recipient> findAllWithQuery(String multifield, String firstname, String lastname, String email,
            Boolean active, Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {

        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Recipient> query = builder.createQuery(Recipient.class);
        Root<Recipient> recipientRoot = query.from(Recipient.class);

        query.select(recipientRoot);
        String columnAttribute = StringUtils.isEmpty(column) ? "lastname" : column;
        List<Predicate> predicates = getQueryPredicates(builder, recipientRoot, multifield, firstname, lastname,
                email, active, currentLocalAuthUuid);

        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(recipientRoot.get(columnAttribute))
                        : builder.desc(recipientRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private String generateToken(Recipient recipient) {
        try {
            StringBuilder sb = new StringBuilder(recipient.getEmail());
            sb.append(Instant.now().getEpochSecond());
            sb.append(new Random().nextLong());
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes("UTF-8"));

            StringBuilder token = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                token.append(String.format("%02x", b & 0xff));
            }
            return token.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("No SHA-256 algorithm found");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported UTF-8 encoding");
        }
        return "default";
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<Recipient> recipientRoot, String multifield,
            String firstname, String lastname, String email, Boolean active, String currentLocalAuthUuid) {

        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(multifield)) {
            predicates.add(
                    builder.or(
                            builder.like(builder.lower(recipientRoot.get("firstname")), "%" + multifield.toLowerCase() + "%"),
                            builder.like(builder.lower(recipientRoot.get("lastname")),
                                    "%" + multifield.toLowerCase() + "%"),
                            builder.like(builder.lower(recipientRoot.get("email")),
                                    "%" + multifield.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(firstname))
            predicates.add(
                    builder.and(builder.like(builder.lower(recipientRoot.get("firstname")), "%" + firstname.toLowerCase() + "%")));

        if (StringUtils.isNotBlank(lastname))
            predicates.add(
                    builder.and(builder.like(builder.lower(recipientRoot.get("lastname")), "%" + lastname.toLowerCase() + "%")));

        if (StringUtils.isNotBlank(email))
            predicates.add(
                    builder.and(builder.like(builder.lower(recipientRoot.get("email")), "%" + email.toLowerCase() + "%")));

        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, Recipient> LocalAuthorityJoin = recipientRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        if (active != null)
            predicates.add(builder.and(builder.equal(recipientRoot.get("active"), active)));

        return predicates;
    }
}
