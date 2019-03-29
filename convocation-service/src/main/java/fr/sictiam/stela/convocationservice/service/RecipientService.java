package fr.sictiam.stela.convocationservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.convocationservice.dao.RecipientRepository;
import fr.sictiam.stela.convocationservice.model.ImportResult;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.csv.RecipientBean;
import fr.sictiam.stela.convocationservice.model.exception.InvalidEmailAddressException;
import fr.sictiam.stela.convocationservice.model.exception.MissingParameterException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.model.exception.RecipientExistsException;
import fr.sictiam.stela.convocationservice.model.util.ConvocationBeanUtils;
import fr.sictiam.stela.convocationservice.service.util.EmailChecker;
import fr.sictiam.stela.convocationservice.service.util.NullableRegexProcessor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.supercsv.cellprocessor.constraint.NotNull;
import org.supercsv.cellprocessor.constraint.StrRegEx;
import org.supercsv.cellprocessor.ift.CellProcessor;
import org.supercsv.exception.SuperCsvCellProcessorException;
import org.supercsv.io.CsvBeanReader;
import org.supercsv.io.ICsvBeanReader;
import org.supercsv.prefs.CsvPreference;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import java.io.IOException;
import java.io.InputStreamReader;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class RecipientService {

    private static Logger LOGGER = LoggerFactory.getLogger(RecipientService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final RecipientRepository recipientRepository;

    private final LocalAuthorityService localAuthorityService;

    private final AssemblyTypeService assemblyTypeService;

    private final ExternalRestService externalRestService;

    public RecipientService(
            RecipientRepository recipientRepository,
            LocalAuthorityService localAuthorityService,
            AssemblyTypeService assemblyTypeService,
            ExternalRestService externalRestService) {
        this.recipientRepository = recipientRepository;
        this.localAuthorityService = localAuthorityService;
        this.assemblyTypeService = assemblyTypeService;
        this.externalRestService = externalRestService;
    }

    public Recipient create(Recipient recipient, String localAuthorityUuid, Boolean force) {

        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthorityUuid);

        return create(recipient, localAuthority, force);
    }

    public Recipient create(Recipient recipient, LocalAuthority localAuthority, Boolean force) {

        if (StringUtils.isBlank(recipient.getFirstname()))
            throw new MissingParameterException("firstname");

        if (StringUtils.isBlank(recipient.getLastname()))
            throw new MissingParameterException("lastname");

        if (StringUtils.isBlank(recipient.getEmail()))
            throw new MissingParameterException("email");

        Optional<Recipient> exist = recipientRepository.findByEmailAndLocalAuthorityUuid(recipient.getEmail(),
                localAuthority.getUuid());
        if (exist.isPresent()) {
            LOGGER.error("A recipient with email {} already exists in local authority {}", recipient.getEmail(), localAuthority.getName());
            throw new RecipientExistsException(exist.get().getActive() ? "convocation.errors.recipient.alreadyExists" : "convocation.errors.recipient.alreadyExistsAndDeactivated");
        }

        if (!force && !EmailChecker.isValid(recipient.getEmail())) {
            LOGGER.error("email {} does not seem to exist", recipient.getEmail());
            throw new InvalidEmailAddressException();
        }

        recipient.setFirstname(StringUtils.capitalize(recipient.getFirstname().toLowerCase().trim()));
        recipient.setLastname(recipient.getLastname().toUpperCase().trim());
        recipient.setLocalAuthority(localAuthority);
        recipient.setActive(true);
        recipient.setToken(generateToken(recipient));
        recipient.setAssemblyTypes(new HashSet<>());
        return save(recipient);
    }

    public Recipient update(String uuid, String localAuthorityUuid, Recipient recipientParams, Boolean force) {

        Recipient recipient = getRecipient(uuid, localAuthorityUuid);


        if (StringUtils.isNotEmpty(recipientParams.getEmail())) {
            if (recipientRepository.recipientExists(uuid, recipient.getLocalAuthority().getUuid(),
                    recipientParams.getEmail()) > 0) {
                LOGGER.error("A recipient with email {} already exists in local authority {}", recipientParams.getEmail(), recipient.getLocalAuthority().getName());
                throw new RecipientExistsException(recipientParams.getActive() ? "convocation.errors.recipient.alreadyExists" : "convocation.errors.recipient.alreadyExistsAndDeactivated");
            }

            if (!recipientParams.getEmail().equals(recipient.getEmail()) && !force && !EmailChecker.isValid(recipientParams.getEmail())) {
                LOGGER.error("email {} does not seem to exist", recipientParams.getEmail());
                throw new InvalidEmailAddressException();
            }
        }

        ConvocationBeanUtils.mergeProperties(recipientParams, recipient, "uuid", "token", "localAuthority", "inactivityDate");

        if (recipientParams.getFirstname() != null)
            recipient.setFirstname(StringUtils.capitalize(recipientParams.getFirstname().toLowerCase()));

        if (recipientParams.getLastname() != null)
            recipient.setLastname(recipientParams.getLastname().toUpperCase());

        if (recipientParams.getActive() != null) {
            if (recipientParams.getActive()) {
                recipient.setInactivityDate(null);
            } else {
                recipient.setInactivityDate(LocalDateTime.now());
                assemblyTypeService.removeRecipient(localAuthorityUuid, recipient);
            }
        }

        return save(recipient);
    }

    public Recipient getRecipient(String uuid, String localAuthorityUuid) {

        return recipientRepository
                .findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid)
                .orElseThrow(() -> new NotFoundException("Recipient " + uuid + " not found in local authority " + localAuthorityUuid));
    }

    public Recipient save(Recipient recipient) {

        return recipientRepository.saveAndFlush(recipient);
    }

    public List<Recipient> getAllByLocalAuthority(String localAuthorityUuid) {

        return recipientRepository.findAllByLocalAuthorityUuidAndActiveTrueOrderByLastname(localAuthorityUuid);
    }

    public Recipient findByProfileinLocalAuthority(String profileUuid, String localAuthorityUuid) {
        JsonNode profile = externalRestService.getProfile(profileUuid);
        if (profile == null) {
            LOGGER.error("Cannot open convocation, unknown profile {}", profileUuid);
        }
        String email = profile.get("agent").get("email").asText();
        return recipientRepository
                .findByEmailAndLocalAuthorityUuid(email, localAuthorityUuid)
                .orElseThrow(() -> new NotFoundException("Recipient with profile " + profileUuid + " cannot be found in " +
                        "local authority " + localAuthorityUuid));
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

        List<Order> orders = new ArrayList<>();

        if (!columnAttribute.equals("active")) {
            orders.add(builder.desc(recipientRoot.get("active")));
        }
        orders.add(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                ? (columnAttribute.equals("active") ? builder.desc(recipientRoot.get(columnAttribute)) :
                builder.asc(recipientRoot.get(columnAttribute)))
                : (columnAttribute.equals("active") ? builder.asc(recipientRoot.get(columnAttribute)) :
                builder.desc(recipientRoot.get(columnAttribute))));
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(orders);

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

    public void deactivateAll(String localAuthorityUuid) {

        List<Recipient> recipients = getAllByLocalAuthority(localAuthorityUuid);
        LocalDateTime now = LocalDateTime.now();
        recipients.forEach(recipient -> {
            recipient.setActive(false);
            recipient.setInactivityDate(now);
            recipientRepository.save(recipient);
        });
    }

    public ImportResult importRecipients(String localAuthorityUuid, MultipartFile recipients) throws IOException {

        LocalAuthority localAuthority = localAuthorityService.getByUuid(localAuthorityUuid);
        int currentLine, errorsCount = currentLine = 0;
        List<ImportResult.Error> errors = new ArrayList<>();
        try (ICsvBeanReader beanReader = new CsvBeanReader(new InputStreamReader(recipients.getInputStream()),
                CsvPreference.EXCEL_NORTH_EUROPE_PREFERENCE)) {

            // the header elements are used to map the values to the bean (names must match)
            final String[] headers = beanReader.getHeader(true);

            final CellProcessor[] processors = getProcessors();
            RecipientBean bean = null;
            do {
                try {
                    currentLine = beanReader.getLineNumber();
                    bean = beanReader.read(RecipientBean.class, RecipientBean.fields(), processors);
                    if (bean != null) {
                        create(new Recipient(bean), localAuthority, true);
                    } else {
                        // End of file
                        break;
                    }
                } catch (SuperCsvCellProcessorException e) {
                    errorsCount++;
                    Pair<String, String> pair = parseException(e);
                    errors.add(new ImportResult.Error(currentLine, pair.getFirst(), pair.getSecond()));

                    LOGGER.error("Failed to import recipient: {} : {}", e.getCsvContext(), e.getMessage());
                } catch (RecipientExistsException e) {
                    errorsCount++;
                    errors.add(new ImportResult.Error(currentLine, bean.getEmail(), "convocation.errors.recipient.alreadyExists"));
                    LOGGER.error("Failed to import recipient: {}. Email already exists", bean);
                }
            } while (true);
        } catch (IOException e) {
            LOGGER.error("Error while importing recipients: {}", e.getMessage());
        }

        return new ImportResult(currentLine - 1, errorsCount, errors);
    }

    private Pair<String, String> parseException(SuperCsvCellProcessorException e) {

        if (e.getProcessor() instanceof NotNull) {
            // Search null field
            List<Object> values = e.getCsvContext().getRowSource();
            if (values.get(0) == null) return Pair.of("lastname", "convocation.errors.csv.mandatory");
            if (values.get(1) == null) return Pair.of("firstname", "convocation.errors.csv.mandatory");
            if (values.get(3) == null) return Pair.of("email", "convocation.errors.csv.mandatory");

        } else if (e.getProcessor() instanceof NullableRegexProcessor) {
            return Pair.of(e.getCsvContext().getRowSource().get(e.getCsvContext().getColumnNumber() - 1).toString(),
                    "convocation.errors.csv.phoneNumberBadFormat");

        } else if (e.getProcessor() instanceof StrRegEx) {
            Pattern nullRegex = Pattern.compile("this processor does not accept null input");
            Pattern badRegex = Pattern.compile(" does not match the regular expression");
            Matcher badRegexMatcher = badRegex.matcher(e.getMessage());
            if (nullRegex.matcher(e.getMessage()).find()) {
                return Pair.of("email", "convocation.errors.csv.mandatory");
            } else if (badRegex.matcher(e.getMessage()).find()) {
                return Pair.of(e.getCsvContext().getRowSource().get(e.getCsvContext().getColumnNumber() - 1).toString(),
                        "convocation.errors.csv.emailBadFormat");
            }
        }
        return Pair.of("Unknown", "Unknown");
    }

    private CellProcessor[] getProcessors() {

        final String emailRegex = "(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"" +
                "(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])";

        final String phoneNumerRegex = "^(\\+33|0)\\d{9}$";

        return new CellProcessor[]{
                new NotNull(), // firstname
                new NotNull(), // lastname
                new org.supercsv.cellprocessor.Optional(), // epci if defined
                new StrRegEx(emailRegex),
                new NullableRegexProcessor(phoneNumerRegex)
        };
    }
}
