package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.exceptions.HistoryNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesCreationException;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesSendException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpSession;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.commands.scan.result.ScanResult.OK;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.persistence.criteria.Subquery;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class PesAllerService implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAllerService.class);

    @PersistenceContext
    private EntityManager entityManager;

    private final PesAllerRepository pesAllerRepository;
    private final PesHistoryRepository pesHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;
    private final DefaultFtpSessionFactory defaultFtpSessionFactory;

    @Value("${application.clamav.port}")
    private Integer clamavPort;

    @Value("${application.clamav.host}")
    private String clamavHost;

    private ClamavClient clamavClient;

    @Autowired
    public PesAllerService(PesAllerRepository pesAllerRepository, PesHistoryRepository pesHistoryRepository,
            ApplicationEventPublisher applicationEventPublisher, LocalAuthorityService localAuthorityService,
            DefaultFtpSessionFactory defaultFtpSessionFactory) {
        this.pesAllerRepository = pesAllerRepository;
        this.pesHistoryRepository = pesHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;
        this.defaultFtpSessionFactory = defaultFtpSessionFactory;

    }

    @PostConstruct
    private void init() {
        clamavClient = new ClamavClient(clamavHost, clamavPort);
    }

    public Long countAllWithQuery(String multifield, String objet, LocalDate creationFrom, LocalDate creationTo,
            StatusType status, String currentLocalAuthUuid) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<PesAller> pesRoot = query.from(PesAller.class);

        List<Predicate> predicates = getQueryPredicates(builder, pesRoot, multifield, objet, creationFrom, creationTo,
                status, currentLocalAuthUuid);
        query.select(builder.count(pesRoot));
        query.where(predicates.toArray(new Predicate[predicates.size()]));

        return entityManager.createQuery(query).getSingleResult();
    }

    public List<PesAller> getAllWithQuery(String multifield, String objet, LocalDate creationFrom, LocalDate creationTo,
            StatusType status, Integer limit, Integer offset, String column, String direction,
            String currentLocalAuthUuid) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<PesAller> query = builder.createQuery(PesAller.class);
        Root<PesAller> pesRoot = query.from(PesAller.class);

        String columnAttribute = StringUtils.isEmpty(column) ? "creation" : column;
        List<Predicate> predicates = getQueryPredicates(builder, pesRoot, multifield, objet, creationFrom, creationTo,
                status, currentLocalAuthUuid);
        query.where(predicates.toArray(new Predicate[predicates.size()]))
                .orderBy(!StringUtils.isEmpty(direction) && direction.equals("ASC")
                        ? builder.asc(pesRoot.get(columnAttribute))
                        : builder.desc(pesRoot.get(columnAttribute)));

        return entityManager.createQuery(query).setFirstResult(offset).setMaxResults(limit).getResultList();
    }

    private List<Predicate> getQueryPredicates(CriteriaBuilder builder, Root<PesAller> pesRoot, String multifield,
            String objet, LocalDate creationFrom, LocalDate creationTo, StatusType status,
            String currentLocalAuthUuid) {
        List<Predicate> predicates = new ArrayList<>();
        if (StringUtils.isNotBlank(multifield)) {
            predicates.add(
                    builder.or(builder.like(builder.lower(pesRoot.get("objet")), "%" + multifield.toLowerCase() + "%"),
                            builder.like(builder.lower(pesRoot.get("comment")), "%" + multifield.toLowerCase() + "%")));
        }
        if (StringUtils.isNotBlank(objet))
            predicates.add(
                    builder.and(builder.like(builder.lower(pesRoot.get("objet")), "%" + objet.toLowerCase() + "%")));
        if (StringUtils.isNotBlank(currentLocalAuthUuid)) {
            Join<LocalAuthority, PesAller> LocalAuthorityJoin = pesRoot.join("localAuthority");
            LocalAuthorityJoin.on(builder.equal(LocalAuthorityJoin.get("uuid"), currentLocalAuthUuid));
        }

        if (creationFrom != null && creationTo != null)
            predicates.add(builder.and(builder.between(pesRoot.get("creation"), creationFrom.atStartOfDay(),
                    creationTo.plusDays(1).atStartOfDay())));

        if (status != null) {
            // TODO: Find a way to do a self left join using a CriteriaQuery instead of a
            // native one
            Query q = entityManager.createNativeQuery(
                    "select ah1.pes_uuid from pes_history ah1 left join pes_history ah2 on (ah1.pes_uuid = ah2.pes_uuid and ah1.date < ah2.date) where ah2.date is null and ah1.status = '"
                            + status + "'");
            List<String> pesHistoriesPesUuids = q.getResultList();
            if (pesHistoriesPesUuids.size() > 0)
                predicates.add(builder.and(pesRoot.get("uuid").in(pesHistoriesPesUuids)));
            else
                predicates.add(builder.and(pesRoot.get("uuid").isNull()));
        }

        return predicates;
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        PesAller pes = getByUuid(event.getPesHistory().getPesUuid());
        pes.getPesHistories().add(event.getPesHistory());
        pesAllerRepository.save(pes);
    }

    public PesAller populateFromByte(PesAller pesAller, byte[] file) {

        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.parse(new ByteArrayInputStream(file));

            XPathFactory xpf = XPathFactory.newInstance();
            XPath path = xpf.newXPath();

            pesAller.setFileType(path.evaluate("/PES_Aller/Enveloppe/Parametres/TypFic/@V", document));
            pesAller.setFileName(path.evaluate("/PES_Aller/Enveloppe/Parametres/NomFic/@V", document));
            pesAller.setColCode(path.evaluate("/PES_Aller/EnTetePES/CodCol/@V", document));
            pesAller.setPostId(path.evaluate("/PES_Aller/EnTetePES/IdPost/@V", document));
            pesAller.setBudCode(path.evaluate("/PES_Aller/EnTetePES/CodBud/@V", document));

        } catch (IOException | XPathExpressionException | ParserConfigurationException | SAXException e) {
            throw new PesCreationException();
        }
        return pesAller;

    }

    public PesAller populateFromFile(PesAller pesAller, MultipartFile file) {

        try {
            Attachment attachment = new Attachment(file.getBytes(), file.getOriginalFilename(), file.getSize());
            pesAller.setAttachment(attachment);
            pesAller.setCreation(LocalDateTime.now());

            populateFromByte(pesAller, file.getBytes());

        } catch (IOException e) {
            throw new PesCreationException();
        }
        return pesAller;

    }

    public PesAller create(String currentProfileUuid, String currentLocalAuthUuid, PesAller pesAller) {
        pesAller.setLocalAuthority(localAuthorityService.getByUuid(currentLocalAuthUuid));
        pesAller.setProfileUuid(currentProfileUuid);

        pesAller = pesAllerRepository.save(pesAller);
        updateStatus(pesAller.getUuid(), StatusType.CREATED);
        return pesAller;
    }

    public PesAller getByUuid(String uuid) {
        return pesAllerRepository.findById(uuid).orElseThrow(PesNotFoundException::new);
    }

    List<PesAller> getPendingSinature() {
        return pesAllerRepository.findByPjFalseAndSignedFalseAndLocalAuthoritySesileSubscriptionTrueAndArchiveNull();
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus, String messsage) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), messsage);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus, byte[] file, String fileName) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), file, fileName);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus, byte[] file, String fileName, String message) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), file, fileName, message);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public boolean checkVirus(byte[] file) throws ClamavException, IOException {
        ScanResult mainResult = clamavClient.scan(new ByteArrayInputStream(file));
        boolean status = false;
        if (!mainResult.equals(OK.INSTANCE)) {
            status = true;
        }
        return status;
    }

    public PesAller save(PesAller pes) {
        return pesAllerRepository.save(pes);
    }

    public Optional<PesAller> getByFileName(String fileName) {
        return pesAllerRepository.findByFileName(fileName);
    }

    // FIXME: only works when PES have 1 only status, cd PesServiceIntegrationTests.testBlockedFluxMultiStatus
    public List<PesAller> getBlockedFlux() {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PesAller> query = cb.createQuery(PesAller.class);
        Root<PesAller> pesTable = query.from(PesAller.class);
        query.select(pesTable);

        Subquery<PesHistory> subquery = query.subquery(PesHistory.class);
        Root<PesHistory> historyTable = subquery.from(PesHistory.class);
        subquery
                .select(historyTable.get("pesUuid")).distinct(true)
                .where(historyTable.get("status")
                        .in(Arrays.asList(StatusType.MAX_RETRY_REACH, StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED)));

        Subquery<PesHistory> subquery2 = query.subquery(PesHistory.class);
        Root<PesHistory> historyTable2 = subquery2.from(PesHistory.class);
        subquery2
                .select(historyTable2.get("pesUuid")).distinct(true)
                .where(historyTable2.get("status")
                        .in(Arrays.asList(StatusType.SENT, StatusType.RESENT, StatusType.MANUAL_RESENT)));

        List<Predicate> mainQueryPredicates = new ArrayList<Predicate>();
        mainQueryPredicates.add(cb.not(pesTable.get("uuid").in(subquery)));
        mainQueryPredicates.add(pesTable.get("uuid").in(subquery2));
        mainQueryPredicates.add(cb.equal(pesTable.get("imported"), false));

        query.where(mainQueryPredicates.toArray(new Predicate[]{}));
        TypedQuery<PesAller> typedQuery = entityManager.createQuery(query);
        List<PesAller> resultList = typedQuery.getResultList();

        return resultList;
    }

    public List<PesHistory> getPesHistoryByTypes(String uuid, List<StatusType> statusTypes) {
        return pesHistoryRepository.findBypesUuidAndStatusInOrderByDateDesc(uuid, statusTypes);
    }

    public PesHistory getLastSentHistory(String uuid) {
        return pesHistoryRepository
                .findBypesUuidAndStatusInOrderByDateDesc(uuid, Arrays.asList(StatusType.SENT, StatusType.RESENT))
                .get(0);
    }

    public PesHistory getHistoryByUuid(String uuid) {
        return pesHistoryRepository.findByUuid(uuid).orElseThrow(HistoryNotFoundException::new);
    }

    public void manualResend(String pesUuid) {
        PesAller pes = getByUuid(pesUuid);
        send(pes);
        StatusType statusType = StatusType.MANUAL_RESENT;
        updateStatus(pes.getUuid(), statusType);
    }

    public void send(PesAller pes) throws PesSendException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pes.getAttachment().getFile());
        FtpSession ftpSession = defaultFtpSessionFactory.getSession();
        FTPClient ftpClient = ftpSession.getClientInstance();
        try {
            ftpClient.sendSiteCommand("P_DEST " + pes.getLocalAuthority().getServerCode().name());
            ftpClient.sendSiteCommand("P_APPLI GHELPES2");
            ftpClient.sendSiteCommand("P_MSG " + pes.getFileType() + "#" + pes.getColCode() + "#" + pes.getPostId()
                    + "#" + pes.getBudCode());
            ftpSession.write(byteArrayInputStream, pes.getAttachment().getFilename());
        } catch (IOException e) {
            throw new PesSendException();
        }
    }
}
