package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.PesRetourRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesRetour;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.exceptions.PesNotFoundException;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

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

    @Scheduled(fixedRate = 60000)
    public void receive() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        FtpSession ftpSession = defaultFtpSessionFactory.getSession();
        FTPClient ftpClient = ftpSession.getClientInstance();

        FTPFile[] files = ftpClient.listFiles();
        for (FTPFile ftpFile : files) {
            if (ftpFile.isFile()) {
                LOGGER.debug("file RECEIVED : " + ftpFile.getName());
                InputStream inputStream = ftpClient.retrieveFileStream(ftpFile.getName());
                if (ftpFile.getName().contains("ACK")) {
                    readACK(inputStream, ftpFile.getName());
                } else if (ftpFile.isFile() && ftpFile.getName().startsWith("PES2R")) {
                    readPesRetour(inputStream, ftpFile.getName());
                }
            }
        }
        ftpSession.close();
    }

    public void readACK(InputStream inputStream, String ackName)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        LOGGER.debug("ACK RECEIVED : " + ackName);
        byte[] targetArray = new byte[inputStream.available()];
        inputStream.read(targetArray);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(targetArray);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath path = xpf.newXPath();
        Document document = builder.parse(byteArrayInputStream);
        String fileName = path.evaluate("/PES_ACQUIT/Enveloppe/Parametres/NomFic/@V", document);

        PesAller pesAller = pesService.getByFileName(fileName).orElseThrow(PesNotFoundException::new);

        int etatAck = Integer.valueOf(path.evaluate("/PES_ACQUIT/ACQUIT/ElementACQUIT/EtatAck/@V", document));
        if (etatAck == 1) {
            pesService.updateStatus(pesAller.getUuid(), StatusType.ACK_RECEIVED, targetArray, ackName);
        } else {
            String errorMessage = path.evaluate("/PES_ACQUIT/ACQUIT/ElementACQUIT/LibelleAnoAck/@V", document);
            pesService.updateStatus(pesAller.getUuid(), StatusType.NACK_RECEIVED, targetArray, ackName, errorMessage);

        }

    }

    public void readPesRetour(InputStream inputStream, String pesRetourName)
            throws IOException, ParserConfigurationException, SAXException, XPathExpressionException {
        byte[] targetArray = new byte[inputStream.available()];
        inputStream.read(targetArray);
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
            Attachment attachment = new Attachment(targetArray, pesRetourName, targetArray.length);
            PesRetour pesRetour = new PesRetour(attachment, localAuthority);
            pesRetourRepository.save(pesRetour);
        } else {
            String idPost = path.evaluate("/PES_Retour/EnTetePES/IdPost/@V", document);
            // TODO send mail to user of this idpost CF redmine issue #3140

        }

    }

}
