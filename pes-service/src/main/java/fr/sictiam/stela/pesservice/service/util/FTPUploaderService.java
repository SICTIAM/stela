package fr.sictiam.stela.pesservice.service.util;

import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.PesSendException;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

@Service
public class FTPUploaderService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FTPUploaderService.class);

    @Value("${application.ftp.host}")
    private String host;

    @Value("${application.ftp.port}")
    private Integer port;

    @Value("${application.ftp.username}")
    private String username;

    @Value("${application.ftp.password}")
    private String password;

    @Autowired
    StorageService storageService;

    public void uploadFile(PesAller pesAller) throws PesSendException {
        FTPClient ftpClient = null;
        try {
            ftpClient = createFTPClient();
            LOGGER.info("Sending commands before uploading...");
            ftpClient.sendSiteCommand("P_DEST " + pesAller.getLocalAuthority().getServerCode().name());
            ftpClient.sendSiteCommand("P_APPLI GHELPES2");
            ftpClient.sendSiteCommand("P_MSG " + pesAller.getFileType() + "#" + pesAller.getColCode()
                    + "#" + pesAller.getPostId() + "#" + pesAller.getBudCode());

            LOGGER.info("Uploading file {} to FTP server", pesAller.getAttachment().getFilename());
            byte[] attachementContent = storageService.getAttachmentContent(pesAller.getAttachment());
            if (attachementContent == null) {
                LOGGER.warn("Unable to retrieve attachement content for PES Aller {}", pesAller.getUuid());
                throw new PesSendException();
            }
            InputStream input = new ByteArrayInputStream(attachementContent);
            ftpClient.storeFile(pesAller.getAttachment().getFilename(), input);
            int reply = ftpClient.getReplyCode();
            LOGGER.info("Reply from FTP server: {}", reply);
        } catch (IOException e) {
            LOGGER.error("Error while trying to upload file to FTP server: {}", e.getMessage());
            throw new PesSendException("IO exception while trying to upload file to FTP server", e);
        } finally {
            if (ftpClient != null)
                disconnect(ftpClient);
        }
    }

    private FTPClient createFTPClient() throws IOException {
        try {
            LOGGER.info("initializing FTPClient...");
            int reply;
            FTPClient ftpClient = new FTPClient();
            ftpClient.connect(host, port);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                LOGGER.error("Error while trying to connect to FTP server, reply: {}", reply);
            }
            ftpClient.login(username, password);
            reply = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
                ftpClient.disconnect();
                LOGGER.error("Error while trying to login to FTP server, reply: {}", reply);
            }
            ftpClient.enterLocalPassiveMode();
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return ftpClient;
        } catch (IOException e) {
            LOGGER.error("Error while trying to create FTPClient: {}", e.getMessage());
            throw e;
        }
    }

    private void disconnect(FTPClient ftpClient) {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) {
                LOGGER.error("Error while trying to disconnect to FTP server: {}", e.getMessage());
            }
        }
    }
}