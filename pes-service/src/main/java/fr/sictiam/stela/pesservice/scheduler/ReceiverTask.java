package fr.sictiam.stela.pesservice.scheduler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

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

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.PesAllerService;

@Component
public class ReceiverTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiverTask.class);

    @Autowired
    private PesAllerService pesService;

    @Autowired
    private DefaultFtpSessionFactory defaultFtpSessionFactory;

    @Scheduled(fixedRate = 5000)
    public void receive() throws IOException, SAXException, ParserConfigurationException, XPathExpressionException {
        FtpSession ftpSession = defaultFtpSessionFactory.getSession();
        FTPClient ftpClient = ftpSession.getClientInstance();

        FTPFile[] files = ftpClient.listFiles();

        for (FTPFile ftpFile : files) {
            if (ftpFile.isFile() && ftpFile.getName().contains("ACK")) {
                InputStream inputStream = ftpClient.retrieveFileStream(ftpFile.getName());
                readACK(inputStream, ftpFile.getName());
            }
        }
        ftpSession.close();

    }

    public void readACK(InputStream inputStream, String ackName)
            throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {

        byte[] targetArray = new byte[inputStream.available()];
        inputStream.read(targetArray);
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(targetArray);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        XPathFactory xpf = XPathFactory.newInstance();
        XPath path = xpf.newXPath();
        Document document = builder.parse(byteArrayInputStream);
        String fileName = path.evaluate("/PES_ACQUIT/Enveloppe/Parametres/NomFic/@V", document);

        PesAller pesAller = pesService.getByAttachementName(fileName);

        pesService.updateStatus(pesAller.getUuid(), StatusType.ACK_RECEIVED, targetArray, ackName);
    }

}
