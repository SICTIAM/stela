package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.PesHistory;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.migration.MigrationLog;
import fr.sictiam.stela.pesservice.model.migration.MigrationStatus;
import fr.sictiam.stela.pesservice.model.migration.PesMigration;
import fr.sictiam.stela.pesservice.model.util.StreamingInMemoryDestFile;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import net.schmizz.sshj.userauth.keyprovider.KeyProvider;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.PasswordUtils;
import net.schmizz.sshj.xfer.scp.SCPFileTransfer;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import javax.mail.MessagingException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;

@Service
public class MigrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);

    private final PesAllerRepository pesAllerRepository;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final NotificationService notificationService;

    @Value("${application.migration.serverIP}")
    String serverIP;

    @Value("${application.migration.serverUser}")
    String serverUser;

    @Value("${application.migration.privateKeyPath}")
    String privateKeyPath;

    @Value("${application.migration.publicKeyPath}")
    String publicKeyPath;

    @Value("${application.migration.privKeyPassphrase}")
    String privKeyPassphrase;

    @Value("${application.migration.mySQLUser}")
    String mySQLUser;

    @Value("${application.migration.mySQLPassword}")
    String mySQLPassword;

    @Value("${application.migration.mySQLPort}")
    String mySQLPort;

    @Value("${application.migration.database}")
    String database;

    private final String query = getStringResourceFromStream("migration.sql");

    public MigrationService(PesAllerRepository pesAllerRepository, LocalAuthorityRepository localAuthorityRepository, NotificationService notificationService) {
        this.pesAllerRepository = pesAllerRepository;
        this.localAuthorityRepository = localAuthorityRepository;
        this.notificationService = notificationService;
    }

    public void migrateStela2PES(LocalAuthority localAuthority, String siren, String email) {

        MigrationLog migrationLog = new MigrationLog();

        log(migrationLog, "Starting migration for localAuthority " + localAuthority.getName()
                + " (uuid: " + localAuthority.getUuid() + ", siren: " + localAuthority.getSiren() + ")", false);
        if (StringUtils.isNotBlank(siren)) {
            log(migrationLog, "Migration for a specific siren was asked: " + siren, false);
        }
        if (StringUtils.isNotBlank(email)) {
            log(migrationLog, "A copy of these logs will be sent to this email address:  " + email, false);
        }

        localAuthority.setMigrationStatus(MigrationStatus.ONGOING);
        localAuthority = localAuthorityRepository.save(localAuthority);

        SSHClient sshClient = getShellConnexion(migrationLog);
        String proccessedQuery = query.replaceAll("\\{\\{siren}}", StringUtils.isNotBlank(siren) ? siren : localAuthority.getSiren());
        ResultSet resultSet = executeMySQLQuery(proccessedQuery, migrationLog);
        List<PesMigration> pesMigrations = toPesMigration(resultSet, migrationLog);
        importPesMigrations(pesMigrations, localAuthority, sshClient, migrationLog);
        closeShellConnexion(sshClient, migrationLog);

        localAuthority.setMigrationStatus(MigrationStatus.DONE);
        localAuthorityRepository.save(localAuthority);
        log(migrationLog, "Ending migration", false);
        if (StringUtils.isNotBlank(email)) {
            try {
                notificationService.sendMail(email, "Migration report for '" + localAuthority.getName() + "'",
                        migrationLog.getLogs());
                LOGGER.info("Migration report sent to {}", email);
            } catch (MessagingException | IOException e) {
                LOGGER.error("Error while trying to send the migration report");
            }
        }
    }

    private void importPesMigrations(List<PesMigration> pesMigrations, LocalAuthority localAuthority,
            SSHClient sshClient, MigrationLog migrationLog) {
        int i = 0;
        log(migrationLog, pesMigrations.size() + " PES to migrate", false);
        for (PesMigration pesMigration : pesMigrations) {

            byte[] archiveBytes = null;
            if (StringUtils.isNotBlank(pesMigration.getArchivePath())) {
                archiveBytes = downloadFile(sshClient, pesMigration.getArchivePath());
            }
            Attachment pesAllerAttachment = getAttachmentFromArchive(pesMigration.getPesAttachment(), archiveBytes,
                    pesMigration.getPesAttachmentSize(), pesMigration.getCreation());
            SortedSet<PesHistory> pesHistories = new TreeSet<>();

            PesAller pesAller = new PesAller(
                    pesMigration.getCreation(),
                    pesMigration.getObjet(),
                    pesAllerAttachment,
                    pesHistories,
                    localAuthority,
                    null,
                    pesMigration.getComment(),
                    pesMigration.getFile_type(),
                    pesMigration.getCol_code(),
                    pesMigration.getPost_id(),
                    pesMigration.getBud_code(),
                    pesMigration.getFile_name(),
                    "PES_PJ".equals(pesMigration.getFile_type()),
                    "PES_PJ".equals(pesMigration.getFile_type()),
                    null,
                    true);
            pesAller = pesAllerRepository.save(pesAller);

            if (pesMigration.getCreation() != null) {
                pesAller.getPesHistories().add(new PesHistory(pesAller.getUuid(), StatusType.CREATED,
                        pesMigration.getCreation(), null));
            }
            if (pesMigration.getSendDate() != null) {
                pesAller.getPesHistories().add(new PesHistory(pesAller.getUuid(), StatusType.SENT,
                        pesMigration.getSendDate(), null));
            }
            if (pesMigration.getDateAR() != null) {
                byte[] bytesAR = getFileFromTarGz(archiveBytes, pesMigration.getFilenameAR());
                pesAller.getPesHistories().add(new PesHistory(pesAller.getUuid(), StatusType.ACK_RECEIVED,
                        pesMigration.getDateAR(), bytesAR, pesMigration.getFilenameAR()));
            }
            if (pesMigration.getDateANO() != null) {
                byte[] fileANOBytes = null;
                if (StringUtils.isNotBlank(pesMigration.getPathFilenameANO())) {
                    fileANOBytes = downloadFile(sshClient, pesMigration.getPathFilenameANO());
                }
                pesAller.getPesHistories().add(new PesHistory(pesAller.getUuid(), StatusType.NACK_RECEIVED,
                        pesMigration.getDateANO(), fileANOBytes, pesMigration.getPathFilenameANO(),
                        pesMigration.getMessageANO()));
            }
            pesAllerRepository.save(pesAller);
            LOGGER.info("PES with message_id '{}' successfully migrated", pesMigration.getMessage_id());
            log(migrationLog, "PES with message_id '" + pesMigration.getMessage_id() + "' successfully migrated", false);
            i++;
            // TEST
            if (i == 200) break;
        }
        log(migrationLog, i + " PES migrated", false);
    }

    private List<PesMigration> toPesMigration(ResultSet resultSet, MigrationLog migrationLog) {
        List<PesMigration> pesMigrations = new ArrayList<>();
        log(migrationLog, "Extracting the PES data from the request result", false);
        // TODO: Improve with an automated parsing resultSet->pojo
        try {
            int i = 0;
            while (resultSet.next()) {
                pesMigrations.add(new PesMigration(
                        resultSet.getString("message_id"),
                        resultSet.getString("objet"),
                        resultSet.getString("comment"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("creation")),
                        resultSet.getString("file_type"),
                        resultSet.getString("col_code"),
                        resultSet.getString("post_id"),
                        resultSet.getString("bud_code"),
                        resultSet.getString("file_name"),
                        resultSet.getString("pesAttachment"),
                        Long.parseLong(resultSet.getString("pesAttachmentSize")),
                        resultSet.getString("archivePath"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("sendDate")),
                        resultSet.getString("status"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("dateAR")),
                        resultSet.getString("filenameAR"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("dateANO")),
                        resultSet.getString("messageANO"),
                        resultSet.getString("filenameANO"),
                        resultSet.getString("pathFilenameANO")
                ));
                i++;
            }
            log(migrationLog, i + " PES extracted", false);
        } catch (SQLException e) {
            LOGGER.error("Error while mapping the resultSet: {}", e);
            log(migrationLog, "Error while mapping the resultSet", true);
        }
        return pesMigrations;
    }

    private Attachment getAttachmentFromArchive(String filename, byte[] archiveBytes, long size, LocalDateTime fileDate) {
        byte[] fileBytes = getFileFromTarGz(archiveBytes, filename);
        return new Attachment(fileBytes, filename, size, fileDate);
    }

    private LocalDateTime getLocalDateTimeFromTimestamp(String timestamp) {
        return StringUtils.isNotBlank(timestamp) ?
                LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(timestamp)),
                        TimeZone.getDefault().toZoneId()) : null;
    }

    private ResultSet executeMySQLQuery(String processedQuery, MigrationLog migrationLog) {
        try {
            Class.forName("com.mysql.jdbc.Driver");
            Connection connect = DriverManager.getConnection("jdbc:mysql://" +
                    serverIP + ":" + mySQLPort + "/" + database, mySQLUser, mySQLPassword);
            log(migrationLog, "Connection to MySQL server (" + serverIP + ":" + mySQLPort + "/" + database
                    + ") established", false);
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery(processedQuery);
            log(migrationLog, "MySQL query successfully executed", false);
            return resultSet;
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error while loading the jdbc driver: {}", e);
            log(migrationLog, "Error while loading the jdbc driver", true);
        } catch (SQLException e) {
            LOGGER.error("Error while connecting to host: {}", e);
            log(migrationLog, "Error while connecting to host", true);
        }
        return null;
    }

    private String getStringResourceFromStream(String path) {
        try {
            InputStream in = new ClassPathResource(path).getInputStream();
            return getStringResourceFromStream(in);
        } catch (IOException e) {
            LOGGER.error("Could not read the file {} : {}", path, e);
            return null;
        }
    }

    private String getStringResourceFromStream(InputStream inputStream) {
        String content = null;
        try {
            content = IOUtils.toString(inputStream);
            inputStream.close();
        } catch (IOException e) {
            LOGGER.error("Could not read the file content : {}", e);
        }
        return content;
    }

    private SSHClient getShellConnexion(MigrationLog migrationLog) {
        SSHClient sshClient = null;
        try {
            String privateKey = getStringResourceFromStream(new FileInputStream(ResourceUtils.getFile(privateKeyPath)));
            String publicKey = getStringResourceFromStream(new FileInputStream(ResourceUtils.getFile(publicKeyPath)));
            sshClient = new SSHClient();
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(serverIP);
            PasswordFinder passwordFinder = PasswordUtils.createOneOff(privKeyPassphrase.toCharArray());
            KeyProvider keys = sshClient.loadKeys(privateKey, publicKey, passwordFinder);
            sshClient.authPublickey(serverUser, keys);
            log(migrationLog, "SSH connection established with " + serverUser + "@" + serverIP, false);
        } catch (IOException e) {
            LOGGER.error("Could not connect to host {}@{}: {}", serverUser, serverIP, e);
            log(migrationLog, "Could not connect to host with " + serverUser + "@" + serverIP, true);
        }
        return sshClient;
    }

    private void closeShellConnexion(SSHClient sshClient, MigrationLog migrationLog) {
        try {
            log(migrationLog, "Closing SSH connection", false);
            sshClient.close();
        } catch (IOException e) {
            LOGGER.error("Could close SSH connection: {}", e);
            log(migrationLog, "Could close SSH connection", true);
        }
    }

    private String executeCommand(SSHClient sshClient, String command) {
        String stdout = null;
        try {
            Session session = sshClient.startSession();
            Session.Command cmd = session.exec(command);
            stdout = net.schmizz.sshj.common.IOUtils.readFully(cmd.getInputStream()).toString();
            session.close();
        } catch (IOException e) {
            LOGGER.error("Could not execute command \"{}\"on server: {}", command, e);
        }
        return stdout;
    }

    private byte[] downloadFile(SSHClient sshClient, String path) {

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        SCPFileTransfer scpFileTransfer = sshClient.newSCPFileTransfer();
        try {
            scpFileTransfer.download(path, new StreamingInMemoryDestFile(os));
            os.close();
        } catch (IOException e) {
            LOGGER.error("Could not scp download file \"{}\": {}", path, e);
        }
        return os.toByteArray();
    }

    private byte[] getFileFromTarGz(byte[] tarGzBytes, String filename) {
        if (tarGzBytes != null && StringUtils.isNotBlank(filename)) {
            InputStream inputStream = new ByteArrayInputStream(tarGzBytes);
            try {
                TarArchiveInputStream tarArchiveInputStream = new TarArchiveInputStream(new GzipCompressorInputStream(inputStream));
                TarArchiveEntry entry;
                while ((entry = tarArchiveInputStream.getNextTarEntry()) != null) {
                    if (entry.isDirectory()) {
                        continue;
                    }
                    if (entry.getName().equals(filename)) {
                        byte[] buffer = new byte[1024];
                        ByteArrayOutputStream bos = new ByteArrayOutputStream();
                        int len = 0;
                        while ((len = tarArchiveInputStream.read(buffer)) != -1) {
                            bos.write(buffer, 0, len);
                        }
                        bos.close();
                        inputStream.close();
                        return bos.toByteArray();
                    }
                }
                inputStream.close();
            } catch (IOException e) {
                LOGGER.error("Error while decompressing .tar.gz : {}", e);
                return null;
            }
        }
        return null;
    }

    private void log(MigrationLog migrationLog, String str, boolean isError) {
        migrationLog.setLogs(migrationLog.getLogs() + "<br/>" + (isError ? "ERROR" : " INFO") + ": " + str);
        if (!isError) LOGGER.info(str);
    }
}
