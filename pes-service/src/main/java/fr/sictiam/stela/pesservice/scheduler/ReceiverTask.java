package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.PesRetourRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistoryError;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.NotificationService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.mail.MessagingException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class ReceiverTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverTask.class);

    @Autowired
    private PesAllerService pesService;

    @Autowired
    private PesRetourRepository pesRetourRepository;

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @Autowired
    private DefaultFtpSessionFactory defaultFtpSessionFactory;

    @Autowired
    private StorageService storageService;

    @Autowired
    private NotificationService notificationService;

    @Value("${application.receiverTask.hoursWithoutNewFiles}")
    private int hoursWithoutNewFiles;

    @Value("${application.receiverTask.maxWaitingPes}")
    private int maxWaitingPes;

    @Value("${application.receiverTask.alertEmail}")
    private String alertEmail;

    private Long runsWithoutNewFiles = 0L;
    private boolean alertSent = false;

    @Value("${application.ftp.timeout}")
    private Integer timeout;

    @Scheduled(fixedDelay = 60000)
    public void receive() {
        LOGGER.info("Starting receiver task...");
        defaultFtpSessionFactory.setConnectTimeout(timeout);

        FtpSession ftpSession = null;
        FTPClient ftpClient = null;
        List<FTPFile> files = new ArrayList<>();

        try {
            ftpSession = defaultFtpSessionFactory.getSession();
            ftpClient = ftpSession.getClientInstance();

            files.addAll(Arrays.stream(ftpClient.listFiles())
                    .filter(file -> !file.getName().equals(".") && !file.getName().equals(".."))
                    .collect(Collectors.toList()));
        } catch (IllegalStateException | IOException e) {
            LOGGER.error("Error with FTP connection: {} caused by {}", e.getMessage(), e.getCause() != null ?
                    e.getCause().getMessage() : "unknown");
        }

        LOGGER.info("{} files found on FTP server: ", files.size());
        files.forEach(file -> LOGGER.info(" |- {}", file.getName()));

        if (files.isEmpty())
            runsWithoutNewFiles++;

        Long nbOfWaitingPes;
        if (!alertSent
                && runsWithoutNewFiles > 60 * hoursWithoutNewFiles
                && (nbOfWaitingPes = pesService.countPesAllerByStatusTypeAndDate(StatusType.SENT, LocalDateTime.now().minusDays(30))) > maxWaitingPes) {
            LOGGER.warn("No new AR since {} hours and {} PES files waiting (limit is {})",
                    hoursWithoutNewFiles, nbOfWaitingPes, maxWaitingPes);
            try {
                notificationService.sendMail(Collections.singletonList(alertEmail).toArray(new String[0]),
                        "Alerte - Problème potentiel de récupération des ARs PES",
                        String.format("Pas de nouvel AR récupéré depuis plus de %d heures et %d PES en attente",
                                hoursWithoutNewFiles, nbOfWaitingPes));
                alertSent = true;
            } catch (IOException | MessagingException e) {
                LOGGER.error("Unable to send email alert for ARs retrieval", e);
            }
        }

        for (FTPFile ftpFile : files) {
            if (ftpFile.isFile()) {
                String fileName = ftpFile.getName();
                LOGGER.debug("file found: " + fileName);
                if ((ftpFile.getName().contains("ACK") || ftpFile.getName().startsWith("PES2R")) && ftpClient != null) {
                    InputStream inputStream = null;
                    try {
                        inputStream = ftpClient.retrieveFileStream(ftpFile.getName());
                        if (ftpClient.completePendingCommand()) {
                            byte[] targetArray = new byte[inputStream.available()];
                            int bytesRead = inputStream.read(targetArray);
                            LOGGER.debug("Read {} bytes from file", bytesRead);
                            if (ftpFile.getName().contains("ACK")) {
                                readACK(targetArray, fileName);
                            } else if (ftpFile.getName().startsWith("PES2R")) {
                                readPesRetour(targetArray, fileName);
                            }
                        }
                    } catch (ParserConfigurationException | XPathExpressionException | SAXException e) {
                        LOGGER.error("Error while parsing XML file: {}", e.getMessage());
                    } catch (SocketTimeoutException e) {
                        LOGGER.error("Error while retrieving file {} on FTP: {}", ftpFile.getName(), e.getMessage());
                    } catch (IOException e) {
                        LOGGER.error("Error while reading the file: {}", e.getMessage());
                    } catch (Exception e) {
                        LOGGER.error("Unexpected error on FTP file processing: {}", e.getMessage());
                    } finally {
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e) {
                                LOGGER.warn("Got an error while closing the input stream : {}", e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        if (ftpClient != null) {
            try {
                ftpClient.disconnect();
            } catch (IOException e) {
                LOGGER.error("Error while disconnecting FTP client", e);
            }
        }

        if (ftpSession != null)
            ftpSession.close();
    }

    public void readACK(byte[] targetArray, String ackName)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        LOGGER.debug("Received ack file : {}", ackName);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(targetArray);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath path = xpf.newXPath();
        Document document = builder.parse(byteArrayInputStream);
        String fileName = path.evaluate("/PES_ACQUIT/Enveloppe/Parametres/NomFic/@V", document);
        LOGGER.debug("Extracted PES filename : {}", fileName);

        PesAller pesAller = pesService.getByFileName(fileName).orElseThrow(PesNotFoundException::new);

        boolean isAckOk = true;
        List<PesHistoryError> errors = new ArrayList<>();
        NodeList nodes = (NodeList) path.evaluate("/PES_ACQUIT/ACQUIT/ElementACQUIT", document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            boolean isCurrentNodeValid = path.evaluate("EtatAck/@V", node).equals("1");
            isAckOk = isAckOk && isCurrentNodeValid;

            if (isCurrentNodeValid)
                continue;

            NodeList innerNodes = (NodeList) path.evaluate("DetailPiece", node, XPathConstants.NODESET);
            if (innerNodes.getLength() != 0) {
                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Node in = innerNodes.item(j);
                    NodeList detailLigneNode = (NodeList) path.evaluate("DetailLigne", in, XPathConstants.NODESET);
                    if (detailLigneNode.getLength() != 0) {
                        /*
                         * Error pattern #1
                         *
                         * <DetailPiece>
                         *     <NumPiece V="20"/>
                         *     <DetailLigne>
                         *         <NumLigne V="1"/>
                         *         <Erreur>
                         *             <NumAnoAck V="ERREUR_AUTRE"/>
                         *             <LibelleAnoAck V="1971 - Compte fonction présent à tort"/>
                         *         </Erreur>
                         *     </DetailLigne>
                         *     [...]
                         * </DetailPiece>
                         * <DetailPiece>
                         *     [...]
                         * </DetailPiece>
                         */
                        for (int k = 0; k < detailLigneNode.getLength(); k++) {
                            Node detail = detailLigneNode.item(k);
                            PesHistoryError e = new PesHistoryError(
                                    path.evaluate("Erreur/NumAnoAck/@V", detail),
                                    path.evaluate("Erreur/LibelleAnoAck/@V", detail),
                                    path.evaluate("NumPiece/@V", in)
                            );
                            errors.add(e);
                        }
                    } else {
                        /*
                         * Error pattern #2
                         *
                         * <DetailPiece>
                         *     <NumPiece V="5129"/>
                         *     <Erreur>
                         *         <NumAnoAck V="ERREUR_AUTRE"/>
                         *         <LibelleAnoAck V="2559 - Le code service de la structure publique destinataire est inconnu ou inactif dans Chorus Pro"/>
                         *     </Erreur>
                         * </DetailPiece>
                         */
                        PesHistoryError e = new PesHistoryError(
                                path.evaluate("Erreur/NumAnoAck/@V", in),
                                path.evaluate("Erreur/LibelleAnoAck/@V", in),
                                path.evaluate("NumPiece/@V", in)
                        );
                        errors.add(e);
                    }
                }
            } else {
                /*
                 * Error pattern #3
                 *
                 * <IdUnique V="TI00064567"/>
                 * <Erreur>
                 *     <NumAnoAck V="ERREUR_DOUBLON_PJ"/>
                 *     <LibelleAnoAck V="1984 - L'identifiant unique de la PJ a déjà été intégré dans HELIOS"/>
                 * </Erreur>
                 */
                PesHistoryError e = new PesHistoryError(
                        path.evaluate("Erreur/NumAnoAck/@V", node),
                        path.evaluate("Erreur/LibelleAnoAck/@V", node),
                        path.evaluate("IdUnique/@V", node)
                );
                errors.add(e);
            }
        }

        if (isAckOk) {
            pesService.updateStatus(pesAller.getUuid(), StatusType.ACK_RECEIVED, targetArray, ackName);
        } else {
            pesService.updateStatus(pesAller.getUuid(), StatusType.NACK_RECEIVED, targetArray, ackName, errors);
        }

    }

    public void readPesRetour(byte[] targetArray, String pesRetourName)
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(targetArray);

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath path = xpf.newXPath();
        Document document = builder.parse(byteArrayInputStream);
        String siret = path.evaluate("/PES_Retour/EnTetePES/IdColl/@V", document);
        Optional<LocalAuthority> localAuthorityOpt = localAuthorityService.getBySirenOrSirens(siret.substring(0, 9));
        if (localAuthorityOpt.isPresent()) {
            LocalAuthority localAuthority = localAuthorityOpt.get();
            Attachment attachment = storageService.createAttachment(pesRetourName, targetArray);
            PesRetour pesRetour = new PesRetour(attachment, localAuthority);
            pesRetourRepository.save(pesRetour);
        } else {
            LOGGER.warn("Got a PES Retour for an unknown SIRET : {}", siret);
            String idPost = path.evaluate("/PES_Retour/EnTetePES/IdPost/@V", document);
            // TODO send mail to user of this idpost CF redmine issue #3140
        }

    }

}
