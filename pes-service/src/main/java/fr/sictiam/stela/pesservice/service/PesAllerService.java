package fr.sictiam.stela.pesservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesExportRepository;
import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.model.*;
import fr.sictiam.stela.pesservice.model.event.PesCreationEvent;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.exceptions.HistoryNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesCreationException;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;
import fr.sictiam.stela.pesservice.service.exceptions.PesSendException;
import fr.sictiam.stela.pesservice.service.util.FTPUploaderService;
import fr.sictiam.stela.pesservice.service.util.TarGzUtils;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.xml.security.utils.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import xyz.capybara.clamav.ClamavClient;
import xyz.capybara.clamav.ClamavException;
import xyz.capybara.clamav.commands.scan.result.ScanResult;
import xyz.capybara.clamav.commands.scan.result.ScanResult.OK;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.*;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;


@Service
public class PesAllerService implements ApplicationListener<PesCreationEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesAllerService.class);

    public static String DEFAULT_GROUP_NAME = "Service PES";

    private static List<StatusType> fatalErrorStatuses =
            Arrays.asList(StatusType.CLASSEUR_DELETED, StatusType.CLASSEUR_WITHDRAWN,
                    StatusType.FILE_ERROR, StatusType.MAX_RETRY_REACH,
                    StatusType.SIGNATURE_INVALID, StatusType.SIGNATURE_MISSING,
                    StatusType.SIGNATURE_SENDING_ERROR);

    @PersistenceContext
    private EntityManager entityManager;

    private final PesAllerRepository pesAllerRepository;
    private final PesHistoryRepository pesHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final LocalAuthorityService localAuthorityService;
    private final FTPUploaderService ftpUploaderService;
    private final Environment environment;
    private final ExternalRestService externalRestService;
    private final PesExportRepository pesExportRepository;
    private final StorageService storageService;

    @Value("${application.clamav.port}")
    private Integer clamavPort;

    @Value("${application.clamav.host}")
    private String clamavHost;

    private ClamavClient clamavClient;

    @Autowired
    public PesAllerService(PesAllerRepository pesAllerRepository, PesHistoryRepository pesHistoryRepository,
            ApplicationEventPublisher applicationEventPublisher, LocalAuthorityService localAuthorityService,
            FTPUploaderService ftpUploaderService, Environment environment, StorageService storageService,
            ExternalRestService externalRestService, PesExportRepository pesExportRepository) {
        this.pesAllerRepository = pesAllerRepository;
        this.pesHistoryRepository = pesHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
        this.localAuthorityService = localAuthorityService;
        this.ftpUploaderService = ftpUploaderService;
        this.environment = environment;
        this.externalRestService = externalRestService;
        this.pesExportRepository = pesExportRepository;
        this.storageService = storageService;
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

        query.select(builder.construct(PesAller.class, pesRoot.get("uuid"), pesRoot.get("creation"),
                pesRoot.get("objet"), pesRoot.get("fileType"), pesRoot.get("lastHistoryDate"),
                pesRoot.get("lastHistoryStatus")));

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
            predicates.add(builder.equal(pesRoot.get("lastHistoryStatus"), status));
        }

        return predicates;
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
            LOGGER.error("Error while parsing PES {} : {} : {}", pesAller.getUuid(), e.getClass(), e.getMessage());
            throw new PesCreationException();
        }
        return pesAller;

    }

    public PesAller create(String currentProfileUuid, String currentLocalAuthUuid, PesAller pesAller,
            String filename, byte[] content) throws PesCreationException {

        pesAller.setLocalAuthority(localAuthorityService.getByUuid(currentLocalAuthUuid));
        pesAller.setProfileUuid(currentProfileUuid);

        Attachment attachment = new Attachment(filename, content, content.length, LocalDateTime.now());

        pesAller.setAttachment(attachment);
        pesAller.setCreation(LocalDateTime.now());

        populateFromByte(pesAller, content);

        if (getByFileName(pesAller.getFileName()).isPresent()) {
            throw new PesCreationException("notifications.pes.sent.error.existing_file_name", null);
        }

        pesAller = pesAllerRepository.saveAndFlush(pesAller);
        updateStatus(pesAller.getUuid(), StatusType.CREATION_IN_PROGRESS);
        // trigger event to store attachment
        applicationEventPublisher.publishEvent(new PesCreationEvent(this, pesAller));

        return pesAller;
    }

    public PesAller create(String currentProfileUuid, String currentLocalAuthUuid, PesAller pesAller,
            MultipartFile file) throws PesCreationException {

        try {
            return create(currentProfileUuid, currentLocalAuthUuid, pesAller, file.getOriginalFilename(), file.getBytes());
        } catch (IOException e) {
            LOGGER.error("Failed to read file content : {}", e.getMessage());
            throw new PesCreationException("notifications.pes.sent.error.read_file", e);
        }
    }


    public PesAller getByUuid(String uuid) {
        return pesAllerRepository.findById(uuid).orElseThrow(PesNotFoundException::new);
    }

    List<PesAller.Light> getPendingSinature() {
        return pesAllerRepository.findByPjFalseAndSignedFalseAndLocalAuthoritySesileSubscriptionTrueAndArchiveNull();
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus);
        updateHistory(pesHistory);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus, String message) {
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), message);
        updateHistory(pesHistory);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus, byte[] file, String fileName) {
        updateStatus(pesUuid, updatedStatus, file, fileName, null);
    }

    public void updateStatus(String pesUuid, StatusType updatedStatus, byte[] file, String
            fileName, List<PesHistoryError> errors) {
        Attachment attachment = storageService.createAttachment(fileName, file);
        PesHistory pesHistory = new PesHistory(pesUuid, updatedStatus, LocalDateTime.now(), attachment, errors);
        updateHistory(pesHistory);
        applicationEventPublisher.publishEvent(new PesHistoryEvent(this, pesHistory));
    }

    public void updateStatusAndAttachment(String pesUuid, StatusType updatedStatus, byte[] file) {
        PesAller pes = getByUuid(pesUuid);
        Attachment attachment = pes.getAttachment();
        attachment = storageService.updateAttachment(attachment, file);
        pes.setAttachment(attachment);
        updateStatus(pesUuid, updatedStatus);
    }

    public void updateHistory(PesHistory newPesHistory) {
        PesAller pes = getByUuid(newPesHistory.getPesUuid());

        if (newPesHistory.getStatus() != StatusType.NOTIFICATION_SENT && newPesHistory.getStatus() != StatusType.GROUP_NOTIFICATION_SENT) {
            // do not update last history Pes fields on status NOTIFICATION_SENT|GROUP_NOTIFICATION_SENT
            pes.setLastHistoryDate(newPesHistory.getDate());
            pes.setLastHistoryStatus(newPesHistory.getStatus());
        }
        pes.getPesHistories().add(newPesHistory);
        pesAllerRepository.saveAndFlush(pes);
    }

    public boolean checkVirus(byte[] file) throws ClamavException {
        ScanResult mainResult = clamavClient.scan(new ByteArrayInputStream(file));
        boolean status = false;
        if (!mainResult.equals(OK.INSTANCE)) {
            status = true;
        }
        return status;
    }

    public PesAller save(PesAller pes) {
        return pesAllerRepository.saveAndFlush(pes);
    }

    public Optional<PesAller> getByFileName(String fileName) {
        return pesAllerRepository.findByFileName(fileName);
    }

    public List<String> getBlockedFlux() {

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<String> query = cb.createQuery(String.class);
        Root<PesAller> pesTable = query.from(PesAller.class);
        query.select(pesTable.get("uuid"));

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

        List<Predicate> mainQueryPredicates = new ArrayList<>();
        mainQueryPredicates.add(cb.not(pesTable.get("uuid").in(subquery)));
        mainQueryPredicates.add(pesTable.get("uuid").in(subquery2));
        mainQueryPredicates.add(cb.equal(pesTable.get("imported"), false));

        query.where(mainQueryPredicates.toArray(new Predicate[]{}));
        TypedQuery<String> typedQuery = entityManager.createQuery(query);
        List<String> resultList = typedQuery.getResultList();

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

    private String getSha1FromBytes(byte[] bytes) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            Formatter formatter = new Formatter();
            for (byte b : md.digest(bytes)) formatter.format("%02x", b);
            return formatter.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.error("Error while trying to get sha1 from file: {}", e.getMessage());
            return null;
        }
    }

    private void persistPesExport(PesAller pes) {
        PesExport pesExport = new PesExport(pes.getUuid(), ZonedDateTime.now(), renameFileToSend(pes),
                pes.getAttachment().getSize(), getSha1FromBytes(pes.getAttachment().getContent()), pes.getLocalAuthority().getSiren());
        try {
            JsonNode node = externalRestService.getProfile(pes.getProfileUuid());
            pesExport.setAgentFirstName(node.get("agent").get("given_name").asText());
            pesExport.setAgentName(node.get("agent").get("family_name").asText());
            pesExport.setAgentEmail(node.get("agent").get("email").asText());
        } catch (Exception e) {
            LOGGER.error("Error while retrieving profile infos : {}", e.getMessage());
        }
        pesExportRepository.save(pesExport);
    }

    public void manualResend(String pesUuid) {
        PesAller pes = getByUuid(pesUuid);
        send(pes);
        StatusType statusType = StatusType.MANUAL_RESENT;
        updateStatus(pes.getUuid(), statusType);
    }

    public void manualRepublish(String pesUuid) {
        PesAller pes = getByUuid(pesUuid);
        updateStatus(pes.getUuid(), StatusType.RECREATED);
    }

    public void send(PesAller pes) throws PesSendException {
        LOGGER.info("Sending PES {} ({})...", pes.getObjet(), pes.getUuid());
        ftpUploaderService.uploadFile(pes);
        persistPesExport(pes);
    }

    public List<PesAller> getPesInError(String localAuthorityUuid) {
        int nbDays = Integer.parseInt(environment.getProperty("application.dailymail.retensiondays", "1"));
        return pesAllerRepository.findAllByLocalAuthority_UuidAndLastHistoryStatusAndLastHistoryDateGreaterThan(
                localAuthorityUuid, StatusType.NACK_RECEIVED, LocalDateTime.now().minusDays(nbDays));
    }

    public String getToken(PesAller pes) {
        try {
            StringBuilder sb = new StringBuilder(pes.getUuid());
            sb.append(pes.getLocalAuthority().getUuid());
            sb.append(pes.getObjet());
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(sb.toString().getBytes("UTF-8"));

            StringBuilder token = new StringBuilder(2 * hash.length);
            for (byte b : hash) {
                token.append(String.format("%02x",
                        b & 0xff));
            }
            return token.toString();
        } catch (NoSuchAlgorithmException e) {
            LOGGER.warn("No SHA-256 algorithm found");
        } catch (UnsupportedEncodingException e) {
            LOGGER.warn("Unsupported UTF-8 encoding");
        }
        return "default";
    }

    public String renameFileToSend(PesAller pes) {
        LocalAuthority localAuthority = pes.getLocalAuthority();
        String idColl = externalRestService.getLocalAuthoritySiret(localAuthority.getUuid());

        // Fallback on siren if an error occurred
        if (idColl == null) return localAuthority.getSiren();

        int count = pesHistoryRepository.countSentToday(LocalDate.now().atStartOfDay(),
                LocalDateTime.now());

        StringBuilder sb = new StringBuilder("PESALR2_");
        sb.append(idColl);
        sb.append("_");
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyMMdd");
        sb.append(dateFormatter.format(LocalDate.now()));
        sb.append("_");
        sb.append(String.format("%03d", ++count));
        sb.append(".xml");
        return sb.toString();
    }

    public Long countPesAllerByStatusTypeAndDate(StatusType statusType, LocalDateTime localDateTime) {
        return pesAllerRepository.countByLastHistoryStatusAndLastHistoryDateAfter(statusType, localDateTime);
    }

    /**
     * @return a pair consisting of the generated archive name and the Base64-encoded representation of the archive
     * containing the PES file and the eventually received ACK / NACK file
     */
    public Pair<String, String> generatePesArchiveWithAck(String uuid) throws IOException {
        PesAller pesAller = getByUuid(uuid);
        List<PesHistory> pesHistories = getPesHistoryByTypes(uuid,
                Arrays.asList(StatusType.ACK_RECEIVED, StatusType.NACK_RECEIVED));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

        TarGzUtils.addEntry(pesAller.getAttachment().getFilename(),
                storageService.getAttachmentContent(pesAller.getAttachment()), taos);

        // we only expect to have one of ACK / NACK history entry
        // and even if there is more than one, only the last received one is of interest for us
        if (!pesHistories.isEmpty()) {
            TarGzUtils.addEntry(pesHistories.get(0).getAttachment().getFilename(),
                    storageService.getAttachmentContent(pesHistories.get(0).getAttachment()), taos);
        }

        taos.close();
        baos.close();

        ByteArrayOutputStream archive = TarGzUtils.compress(baos);
        String archiveName = pesAller.getAttachment().getFilename() + ".tar.gz";
        String archiveBase64 = Base64.encode(archive.toByteArray());

        return Pair.of(archiveName, archiveBase64);
    }

    public boolean isAPesOrmc(PesAller pesAller) {
        InputStream attachement = new ByteArrayInputStream(storageService.getAttachmentContent(pesAller.getAttachment()));
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(attachement);
            doc.getDocumentElement().normalize();
            NodeList nodeList = doc.getElementsByTagName("TypFic");

            Node typeFicNode = null;
            Element typeFicElement = null;
            if(nodeList.getLength() > 0) {
                typeFicNode = nodeList.item(0);
                if (typeFicNode.getNodeType() == Node.ELEMENT_NODE) {
                    typeFicElement = (Element) typeFicNode;
                    return typeFicElement.getAttribute("V").equals("PESORMC");
                } else {
                    LOGGER.debug("[isAPesOrmc] Don't found elements with 'TypeFic' tag name");
                    return false;
                }
            } else {
                LOGGER.info("[isAPesOrmc] Don't found nodes with 'TypeFic' tag name");
                return false;
            }
        } catch (ParserConfigurationException e) {
            LOGGER.error("[isAPesOrmc] An error occured while trying to parse xml file {} attachement of pes {}",
                    pesAller.getFileName(),
                    pesAller.getUuid(),
                    e);
        } catch (SAXException | IOException e) {
            LOGGER.error("[isAPesOrmc] An error occured while trying to read xml file {} attachement of pes {}",
                    pesAller.getFileName(),
                    pesAller.getUuid(),
                    e);
        } catch (Exception e) {
            LOGGER.error("[isAPesOrmc] Unexpected exception parsing PES file", e);
        }
        return false;
    }

    public PesAller getByUuidAndLocalAuthorityUuid(String uuid, String localAuthorityUuid) {
        return pesAllerRepository.findByUuidAndLocalAuthorityUuid(uuid, localAuthorityUuid).orElseThrow(PesNotFoundException::new);
    }

    public boolean hasAFatalError(PesAller pesAller) {
        return fatalErrorStatuses.contains(pesAller.getLastHistoryStatus());
    }

    @Override
    public void onApplicationEvent(PesCreationEvent event) {
        Attachment attachment = event.getPesAller().getAttachment();
        storageService.storeAttachment(attachment);
        updateStatus(event.getPesAller().getUuid(), StatusType.CREATED);
    }
}
