package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.Flux;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.Right;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.migration.ActeMigration;
import fr.sictiam.stela.acteservice.model.migration.Migration;
import fr.sictiam.stela.acteservice.model.migration.MigrationLog;
import fr.sictiam.stela.acteservice.model.migration.MigrationStatus;
import fr.sictiam.stela.acteservice.model.migration.MigrationWrapper;
import fr.sictiam.stela.acteservice.model.migration.UserMigration;
import fr.sictiam.stela.acteservice.model.util.StreamingInMemoryDestFile;
import fr.sictiam.stela.acteservice.service.util.DiscoveryUtils;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;
import org.springframework.web.client.RestTemplate;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Service
public class MigrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MigrationService.class);

    private final ActeRepository acteRepository;
    private final LocalAuthorityRepository localAuthorityRepository;
    private final NotificationService notificationService;
    private final DiscoveryUtils discoveryUtils;

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

    private final String sql_actes = getStringResourceFromStream("migration/actes.sql");
    private final String sql_users = getStringResourceFromStream("migration/users.sql");
    private final String sql_local_authority_groups = getStringResourceFromStream("migration/local_authority_groups.sql");

    public MigrationService(ActeRepository acteRepository, LocalAuthorityRepository localAuthorityRepository,
            NotificationService notificationService, DiscoveryUtils discoveryUtils) {
        this.acteRepository = acteRepository;
        this.localAuthorityRepository = localAuthorityRepository;
        this.notificationService = notificationService;
        this.discoveryUtils = discoveryUtils;
    }

    public void migrateStela2Users(LocalAuthority localAuthority, String siren, String email) {
        MigrationLog migrationLog = new MigrationLog();
        log(migrationLog, "Starting users migration for localAuthority " + localAuthority.getName()
                + " (uuid: " + localAuthority.getUuid() + ", siren: " + localAuthority.getSiren() + ")", false);
        if (StringUtils.isNotBlank(siren)) {
            log(migrationLog, "Migration for a specific siren was asked: " + siren, false);
        }
        if (StringUtils.isNotBlank(email)) {
            log(migrationLog, "A copy of these logs will be sent to this email address:  " + email, false);
        }

        if (localAuthority.getMigration() == null) localAuthority.setMigration(new Migration());
        localAuthority.getMigration().setMigrationUsers(MigrationStatus.ONGOING);
        localAuthority = localAuthorityRepository.save(localAuthority);

        List<String> groupIds = getGroupIdsFromSiren(StringUtils.isNotBlank(siren) ? siren : localAuthority.getSiren(),
                migrationLog);
        if (groupIds == null || groupIds.isEmpty()) {
            log(migrationLog, "No groupIds for this localAuthority", false);
        } else {
            String sqlGroupIds = groupIds.stream()
                    .map(groupId -> "gul2.groupid = " + groupId)
                    .collect(Collectors.joining(" OR "));
            String proccessedQuery = sql_users
                    .replaceAll("\\{\\{groupIds}}", sqlGroupIds);
            ResultSet resultSet = executeMySQLQuery(proccessedQuery, migrationLog);
            List<UserMigration> userMigrations = toUsersMigration(resultSet, migrationLog);
            MigrationWrapper migrationWrapper = new MigrationWrapper(userMigrations, "acte",
                    new HashSet<>(Arrays.stream(Right.values()).map(Right::toString).collect(Collectors.toSet())));

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<String> response = restTemplate.postForEntity(discoveryUtils.adminServiceUrl()
                    + "/api/admin/agent/migration/users/{localAuthorityUuid}", migrationWrapper, String.class, localAuthority.getUuid());
            if (!response.getStatusCode().is2xxSuccessful()) {
                log(migrationLog, "Bad response while trying to sending users to the admin-service: "
                        + response.getStatusCode().toString(), true);
            } else {
                log(migrationLog, "Users successfully created", false);
                // TODO Fetch certificates info from the LDAP
                // TODO Send users to Ozwillo
            }
            userMigrations.forEach(userMigration -> LOGGER.debug(userMigration.toString()));
        }

        localAuthority.getMigration().setMigrationUsers(MigrationStatus.DONE);
        localAuthorityRepository.save(localAuthority);
        log(migrationLog, "Ending migration", false);
        processEmail(email, localAuthority.getName(), migrationLog);
    }

    public void migrateStela2Actes(LocalAuthority localAuthority, String siren, String email, String month) {

        // siren 214400152
        MigrationLog migrationLog = new MigrationLog();

        log(migrationLog, "Starting Actes migration for localAuthority " + localAuthority.getName()
                + " (uuid: " + localAuthority.getUuid() + ", siren: " + localAuthority.getSiren() + ")", false);
        if (StringUtils.isNotBlank(siren)) {
            log(migrationLog, "Migration for a specific siren was asked: " + siren, false);
        }
        if (StringUtils.isNotBlank(email)) {
            log(migrationLog, "A copy of these logs will be sent to this email address:  " + email, false);
        }

        if (localAuthority.getMigration() == null) localAuthority.setMigration(new Migration());
        localAuthority.getMigration().setMigrationData(MigrationStatus.ONGOING);
        localAuthority = localAuthorityRepository.save(localAuthority);

        SSHClient sshClient = getShellConnexion(migrationLog);
        String proccessedQuery = sql_actes
                .replaceAll("\\{\\{siren}}", StringUtils.isNotBlank(siren) ? siren : localAuthority.getSiren())
                .replaceAll("\\{\\{month}}", (StringUtils.isNotBlank(month) && Integer.parseInt(month) > 0) ? month : "1");
        ResultSet resultSet = executeMySQLQuery(proccessedQuery, migrationLog);
        List<ActeMigration> acteMigrations = toActesMigration(resultSet, migrationLog);
        importActesMigrations(acteMigrations, localAuthority, sshClient, migrationLog);
        closeShellConnexion(sshClient, migrationLog);

        localAuthority.getMigration().setMigrationData(MigrationStatus.DONE);
        localAuthorityRepository.save(localAuthority);
        log(migrationLog, "Ending migration", false);
        processEmail(email, localAuthority.getName(), migrationLog);
    }

    private void importActesMigrations(List<ActeMigration> acteMigrations, LocalAuthority localAuthority,
            SSHClient sshClient, MigrationLog migrationLog) {
        int i = 0;
        log(migrationLog, acteMigrations.size() + " Actes to migrate", false);
        for (ActeMigration acteMigration : acteMigrations) {

            byte[] archiveBytes = null;
            if (StringUtils.isNotBlank(acteMigration.getArchivePath())) {
                log(migrationLog, "ArchivePath is '" + acteMigration.getArchivePath() + "'", false);
                archiveBytes = downloadFile(sshClient, acteMigration.getArchivePath());
            } else {
                log(migrationLog, "ArchivePath is blank", false);
            }
            if (archiveBytes == null) LOGGER.warn("ArchiveBytes is null");

            Attachment acteAttachment = getAttachmentFromArchive(acteMigration.getActeAttachment(), archiveBytes, 0);
            SortedSet<ActeHistory> acteHistories = new TreeSet<>();

            String code = String.format("%s-%s-%s-%s-%s",
                    acteMigration.getCode_matiere1(),
                    acteMigration.getCode_matiere2(),
                    acteMigration.getCode_matiere3(),
                    acteMigration.getCode_matiere4(),
                    acteMigration.getCode_matiere5()
            );
            Acte acte = new Acte(
                    acteMigration.getNumber(),
                    acteMigration.getCreation(),
                    acteMigration.getDecision(),
                    ActeNature.code(Integer.parseInt(acteMigration.getNature())),
                    code,
                    acteMigration.getCode_label(),
                    acteMigration.getObjet(),
                    acteMigration.isIs_public(),
                    acteMigration.isIs_public_website(),
                    acteAttachment,
                    new ArrayList<>(),
                    acteHistories,
                    localAuthority,
                    true
            );
            acte = acteRepository.save(acte);

            if (acteMigration.getCreation() != null) {
                acte.getActeHistories().add(new ActeHistory(acte.getUuid(), StatusType.CREATED,
                        acteMigration.getCreation(), null, Flux.TRANSMISSION_ACTE));
            } else {
                log(migrationLog, "Acte creation date is null", false);
            }
            if (acteMigration.getSendDate() != null) {
                acte.getActeHistories().add(new ActeHistory(acte.getUuid(), StatusType.SENT,
                        acteMigration.getSendDate(), null, Flux.TRANSMISSION_ACTE));
            } else {
                log(migrationLog, "Acte send date is null", false);
            }
            if (acteMigration.getDateAR() != null) {
                byte[] archiveARBytes = null;
                if (StringUtils.isNotBlank(acteMigration.getArchivePathAR())) {
                    log(migrationLog, "ArchivePathAR is '" + acteMigration.getArchivePathAR() + "'", false);
                    archiveARBytes = downloadFile(sshClient, acteMigration.getArchivePathAR());
                } else {
                    log(migrationLog, "ArchivePathAR is blank", false);
                }

                log(migrationLog, "FilenameAR is '" + acteMigration.getFilenameAR() + "'", false);
                byte[] bytesAR = getFileFromTarGz(archiveARBytes, acteMigration.getFilenameAR());
                if (bytesAR == null) log(migrationLog, "bytesAR is null", false);
                acte.getActeHistories().add(new ActeHistory(acte.getUuid(), StatusType.ACK_RECEIVED,
                        acteMigration.getDateAR(), bytesAR, acteMigration.getFilenameAR()));
            } else {
                log(migrationLog, "Acte AR date is null", false);
            }
            if (acteMigration.getDateANO() != null) {
                byte[] archiveANOBytes = null;
                if (StringUtils.isNotBlank(acteMigration.getArchivePathANO())) {
                    archiveANOBytes = downloadFile(sshClient, acteMigration.getArchivePathANO());
                }
                byte[] fileANOBytes = getFileFromTarGz(archiveANOBytes, acteMigration.getFilenameANO());
                acte.getActeHistories().add(new ActeHistory(acte.getUuid(), StatusType.NACK_RECEIVED,
                        acteMigration.getDateANO(), fileANOBytes, acteMigration.getFilenameANO(),
                        acteMigration.getMessageANO()));
            }
            if (acteMigration.getDateASKCANCEL() != null) {
                byte[] archiveASKCANCELBytes = null;
                if (StringUtils.isNotBlank(acteMigration.getArchivePathASKCANCEL())) {
                    archiveASKCANCELBytes = downloadFile(sshClient, acteMigration.getArchivePathASKCANCEL());
                }
                byte[] fileASKCANCELBytes = getFileFromTarGz(archiveASKCANCELBytes, acteMigration.getFilenameASKCANCEL());
                acte.getActeHistories().add(new ActeHistory(acte.getUuid(), StatusType.CANCELLATION_ASKED,
                        acteMigration.getDateASKCANCEL(), fileASKCANCELBytes, acteMigration.getFilenameASKCANCEL(),
                        Flux.ANNULATION_TRANSMISSION));
            }
            if (acteMigration.getDateARCANCEL() != null) {
                byte[] archiveARCANCELBytes = null;
                if (StringUtils.isNotBlank(acteMigration.getArchivePathARCANCEL())) {
                    archiveARCANCELBytes = downloadFile(sshClient, acteMigration.getArchivePathARCANCEL());
                }
                byte[] fileARCANCELBytes = getFileFromTarGz(archiveARCANCELBytes, acteMigration.getFilenameARCANCEL());
                acte.getActeHistories().add(new ActeHistory(acte.getUuid(), StatusType.CANCELLED,
                        acteMigration.getDateARCANCEL(), fileARCANCELBytes, acteMigration.getArchivePathARCANCEL(),
                        Flux.AR_ANNULATION_TRANSMISSION));
            }
            acteRepository.save(acte);
            LOGGER.info("Acte with form_id '{}' successfully migrated", acteMigration.getForm_id());
            log(migrationLog, "Acte with form_id '" + acteMigration.getForm_id() + "' successfully migrated", false);
            i++;
        }
        log(migrationLog, i + " Actes migrated", false);
    }

    public void resetMigration(String migrationType, String localAuthUuid) {
        LocalAuthority localAuthority = localAuthorityRepository.findByUuid(localAuthUuid).orElse(null);
        if (localAuthority != null) {
            if (localAuthority.getMigration() == null) {
                localAuthority.setMigration(new Migration());
            } else if ("migrationUsers".equals(migrationType)) {
                localAuthority.getMigration().setMigrationUsers(MigrationStatus.NOT_DONE);
            } else if ("migrationData".equals(migrationType)) {
                localAuthority.getMigration().setMigrationData(MigrationStatus.NOT_DONE);
            }
            localAuthorityRepository.save(localAuthority);
        }
    }

    private List<String> getGroupIdsFromSiren(String siren, MigrationLog migrationLog) {
        log(migrationLog, "Fetching local authority groupIds", false);
        String proccessedQuery = sql_local_authority_groups
                .replaceAll("\\{\\{siren}}", siren);
        ResultSet resultSet = executeMySQLQuery(proccessedQuery, migrationLog);
        try {
            resultSet.next();
            String groupIds = resultSet.getString("groupid");
            return Arrays.asList(groupIds.split(","));
        } catch (SQLException e) {
            LOGGER.error("Error while mapping the resultSet: {}", e);
            log(migrationLog, "Error while mapping the resultSet", true);
            return null;
        }
    }

    private List<ActeMigration> toActesMigration(ResultSet resultSet, MigrationLog migrationLog) {
        List<ActeMigration> acteMigrations = new ArrayList<>();
        log(migrationLog, "Extracting the Acte data from the request result", false);
        try {
            int i = 0;
            while (resultSet.next()) {
                acteMigrations.add(new ActeMigration(
                        resultSet.getString("form_id"),
                        resultSet.getString("number"),
                        resultSet.getString("objet"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("creation")),
                        getLocalDateTimeFromTimestamp(resultSet.getString("sendDate")),
                        getLocalDateTimeFromTimestamp(resultSet.getString("decision")).toLocalDate(),
                        resultSet.getString("code_matiere1"),
                        resultSet.getString("code_matiere2"),
                        resultSet.getString("code_matiere3"),
                        resultSet.getString("code_matiere4"),
                        resultSet.getString("code_matiere5"),
                        resultSet.getString("code_label"),
                        resultSet.getBoolean("is_public"),
                        resultSet.getBoolean("is_public_website"),
                        resultSet.getString("nature"),
                        resultSet.getString("archivePath"),
                        resultSet.getString("acteAttachment"),
                        resultSet.getString("annexes"),
                        resultSet.getString("status"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("dateAR")),
                        resultSet.getString("archivePathAR"),
                        resultSet.getString("filenameAR"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("dateANO")),
                        resultSet.getString("archivePathANO"),
                        resultSet.getString("filenameANO"),
                        resultSet.getString("messageANO"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("dateASKCANCEL")),
                        resultSet.getString("archivePathASKCANCEL"),
                        resultSet.getString("filenameASKCANCEL"),
                        getLocalDateTimeFromTimestamp(resultSet.getString("dateASKCANCEL")),
                        resultSet.getString("archivePathARCANCEL"),
                        resultSet.getString("filenameARCANCEL")
                ));
                i++;
            }
            log(migrationLog, i + " Actes extracted", false);
        } catch (SQLException e) {
            LOGGER.error("Error while mapping the resultSet: {}", e);
            log(migrationLog, "Error while mapping the resultSet", true);
        }
        return acteMigrations;
    }

    private List<UserMigration> toUsersMigration(ResultSet resultSet, MigrationLog migrationLog) {
        List<UserMigration> userMigrations = new ArrayList<>();
        log(migrationLog, "Extracting the Users data from the request result", false);
        // TODO: Improve with an automated parsing resultSet->pojo
        try {
            int i = 0;
            while (resultSet.next()) {
                userMigrations.add(new UserMigration(
                        resultSet.getString("name"),
                        resultSet.getString("uname"),
                        resultSet.getString("email")
                ));
                i++;
            }
            log(migrationLog, i + " users extracted", false);
        } catch (SQLException e) {
            LOGGER.error("Error while mapping the resultSet: {}", e);
            log(migrationLog, "Error while mapping the resultSet", true);
        }
        return userMigrations;
    }

    private Attachment getAttachmentFromArchive(String filename, byte[] archiveBytes, long size) {
        byte[] fileBytes = getFileFromTarGz(archiveBytes, filename);
        return new Attachment(fileBytes, filename, size);
    }

    private LocalDateTime getLocalDateTimeFromTimestamp(String timestamp) {
        return StringUtils.isNotBlank(timestamp) ?
                LocalDateTime.ofInstant(Instant.ofEpochSecond(Long.parseLong(timestamp)),
                        TimeZone.getDefault().toZoneId()) : null;
    }

    private ResultSet executeMySQLQuery(String processedQuery, MigrationLog migrationLog) {
        try {
            log(migrationLog, processedQuery, false);
            Class.forName("com.mysql.jdbc.Driver");
            Connection connect = DriverManager.getConnection("jdbc:mysql://" +
                    serverIP + ":" + mySQLPort + "/" + database, mySQLUser, mySQLPassword);
            log(migrationLog, "Connection to MySQL server (" + serverIP + ":" + mySQLPort + "/" + database
                    + ") established", false);
            Statement statement = connect.createStatement();
            ResultSet resultSet = statement.executeQuery(processedQuery);
            log(migrationLog, "MySQL sql_actes successfully executed", false);
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

    private void processEmail(String email, String localAuthorityName, MigrationLog migrationLog) {
        if (StringUtils.isNotBlank(email)) {
            try {
                notificationService.sendMail(email, "Migration report for '" + localAuthorityName + "'",
                        migrationLog.getLogs());
                LOGGER.info("Migration report sent to {}", email);
            } catch (MessagingException | IOException e) {
                LOGGER.error("Error while trying to send the migration report");
            }
        }
    }

    private void log(MigrationLog migrationLog, String str, boolean isError) {
        migrationLog.setLogs(migrationLog.getLogs() + "<br/>" + (isError ? "ERROR" : " INFO") + ": " + str);
        if (!isError) LOGGER.info(str);
    }
}
