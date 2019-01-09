package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
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
import org.apache.commons.io.FileUtils;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
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
    private PesAllerRepository pesAllerRepository;

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

    @Scheduled(fixedRate = 60000)
    public void receive() throws IOException {
        LOGGER.info("Starting receiver task...");
        defaultFtpSessionFactory.setConnectTimeout(timeout);
        defaultFtpSessionFactory.setDataTimeout(timeout);
        defaultFtpSessionFactory.setDefaultTimeout(timeout);

        FtpSession ftpSession = null;
        FTPClient ftpClient = null;
        List<FTPFile> files = new ArrayList<>();

        try {
            ftpSession = defaultFtpSessionFactory.getSession();
            ftpClient = ftpSession.getClientInstance();

            files.addAll(Arrays.asList(ftpClient.listFiles()).stream()
                    .filter(file -> !file.getName().equals(".") && !file.getName().equals(".."))
                    .collect(Collectors.toList()));
        } catch (IllegalStateException | IOException e) {
            LOGGER.error("Error with FTP connection: {} caused by {}", e.getMessage(), e.getCause() != null ?
                    e.getCause().getMessage() : "unknown");
        }

        LOGGER.info("{} files found on FTP server: ", files.size());
        files.forEach(file -> LOGGER.info(" |- {}", file.getName()));

        if (files.size() == 0)
            runsWithoutNewFiles++;

        if (runsWithoutNewFiles > 60 * hoursWithoutNewFiles
                && pesAllerRepository.countByLastHistoryStatus(StatusType.SENT) > maxWaitingPes
                && !alertSent) {
            LOGGER.warn("No new AR since 4 hours and more than 20 PES files waiting");
            try {
                notificationService.sendMail(Collections.singletonList(alertEmail).toArray(new String[0]),
                        "Alerte - Problème potentiel de récupération des ARs PES",
                        "Pas de nouvel AR récupéré depuis plus de 4 heures et plus de 20 PES en attente");
                alertSent = true;
            } catch (MessagingException e) {
                LOGGER.error("Unable to send email alert for ARs retrieval", e);
            }
        }

        for (FTPFile ftpFile : files) {
            if (ftpFile.isFile()) {
                String fileName = ftpFile.getName();
                LOGGER.debug("file found: " + fileName);
                if ((ftpFile.getName().contains("ACK") || ftpFile.getName().startsWith("PES2R")) && ftpClient != null) {
                    InputStream inputStream = ftpClient.retrieveFileStream(ftpFile.getName());
                    if (ftpClient.completePendingCommand()) {
                        byte[] targetArray = new byte[inputStream.available()];
                        inputStream.read(targetArray);
                        try {
                            if (ftpFile.getName().contains("ACK")) {
                                readACK(targetArray, fileName);
                            } else if (ftpFile.getName().startsWith("PES2R")) {
                                readPesRetour(targetArray, fileName);
                            }
                        } catch (IOException | ParserConfigurationException | XPathExpressionException
                                | SAXException e) {
                            LOGGER.error("Error while reading the file: {}", e.getMessage());
                            FileUtils.writeByteArrayToFile(new File(fileName), targetArray);
                        } finally {
                            inputStream.close();
                        }
                    }
                }
            }
        }

        if (ftpSession != null)
            ftpSession.close();
    }

    public void readACK(byte[] targetArray, String ackName)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        LOGGER.debug("ACK RECEIVED : " + ackName);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(targetArray);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath path = xpf.newXPath();
        Document document = builder.parse(byteArrayInputStream);
        String fileName = path.evaluate("/PES_ACQUIT/Enveloppe/Parametres/NomFic/@V", document);
        LOGGER.debug("NomFic : " + fileName);

        PesAller pesAller = pesService.getByFileName(fileName).orElseThrow(PesNotFoundException::new);

        boolean etatAck = true;
        List<PesHistoryError> errors = new ArrayList<>();
        NodeList nodes = (NodeList) path.evaluate("/PES_ACQUIT/ACQUIT/ElementACQUIT", document, XPathConstants.NODESET);
        for (int i = 0; i < nodes.getLength(); i++) {
            Node node = nodes.item(i);
            etatAck = etatAck && path.evaluate("EtatAck/@V", node).equals("1");

            NodeList innerNodes = (NodeList) path.evaluate("DetailPiece", node, XPathConstants.NODESET);
            if (innerNodes.getLength() != 0) {
                for (int j = 0; j < innerNodes.getLength(); j++) {
                    Node in = innerNodes.item(j);
                    PesHistoryError e = new PesHistoryError(
                            path.evaluate("Erreur/NumAnoAck/@V", in),
                            path.evaluate("Erreur/LibelleAnoAck/@V", in),
                            path.evaluate("NumPiece/@V", in)
                    );
                    errors.add(e);
                }
            } else {
                PesHistoryError e = new PesHistoryError(
                        path.evaluate("Erreur/NumAnoAck/@V", node),
                        path.evaluate("Erreur/LibelleAnoAck/@V", node),
                        path.evaluate("IdUnique/@V", node)
                );
                errors.add(e);
            }
        }

        if (etatAck) {
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
            String idPost = path.evaluate("/PES_Retour/EnTetePES/IdPost/@V", document);
            // TODO send mail to user of this idpost CF redmine issue #3140
        }

    }

}
