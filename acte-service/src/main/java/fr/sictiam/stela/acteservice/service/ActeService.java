package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.controller.ActeNotFoundException;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Enveloppe;
import fr.sictiam.stela.acteservice.model.Flux;
import fr.sictiam.stela.acteservice.model.Message;
import fr.sictiam.stela.acteservice.model.StatusType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@Service
public class ActeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeService.class);
    
    private ActeRepository acteRepository;
    private ActeHistoryRepository acteHistoryRepository;

    @Autowired
    public ActeService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository){
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
    }

    /**
     * Create new Acte entity in databaseFilename, compress the files to a tar.gz archive and delivers it to minister.
     * 
     * @param acte Acte's data used to create Acte entity.
     * @param file Acte's file.
     * @param annexes Acte's annexes.
     * 
     * @return The newly created Acte entity.
     */
    public Acte createAndSend(Acte acte, MultipartFile file, MultipartFile[] annexes) throws ActeNotSentException {
        acte.setFile(file.getOriginalFilename());

        Acte created = acteRepository.save(acte);
        ActeHistory event = initHistory(acte);

        created.setCreation(event.getDate());
        updateActeStatus(created, event.getDate(), event.getStatus());

        LOGGER.info("Acte {} created with id {}.", created.getNumber(), created.getUuid());

        try {
            sendActe(created, file, annexes);
            event = updateHistory(acte, StatusType.SENT);
        } catch (Exception e) {
            event = updateHistory(acte, StatusType.NOT_SENT);
            throw new ActeNotSentException(e.getMessage());
        } finally {
            updateActeStatus(created, event.getDate(), event.getStatus());
            LOGGER.info("Acte {}: status set to {}.", created.getNumber(), event.getStatus());
        }
        
        return created;
    }

    public List<Acte> getAll() {
        return acteRepository.findAllByOrderByCreationDesc();
    }

    public Acte getByUuid(String uuid) {
        return acteRepository.findByUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

    /**
     * Initialize a new history.
     * 
     * @param acte The acte which gonna have a glorious history.
     * 
     * @return An ActeHistory object, representing the current state of the acte's history.
     */
    protected ActeHistory initHistory(Acte acte) {
        return acteHistoryRepository.save(new ActeHistory(acte.getUuid(), StatusType.CREATED, new Date()));
    }

    /**
     * Update an acte's history.
     * 
     * @param acte Acte which history gonna be updated.
     * @param statusType The new state of history for the Acte.
     * 
     * @return An ActeHistory object, representing the current state of the acte's acteHistoryRepository.
     */
    protected ActeHistory updateHistory(Acte acte, StatusType statusType) {
        return acteHistoryRepository.save(new ActeHistory(acte.getUuid(), statusType, new Date()));
    }

    /**
     * Update an acte's status.
     * 
     * @param acte Acte which status gonna be updated.
     * @param statusType The new status of the Acte.
     */
    protected void updateActeStatus(Acte acte, Date date, StatusType status) {
        acte.setStatus(status);
        acte.setLastUpdateTime(date);
        acteRepository.save(acte);
    }

    public List<ActeHistory> getActHistory(String uuid) {
        return acteHistoryRepository.findByActeUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

    /**
     * Compress file and annexes into a tar.gz archive and delivers it to minister.
     *
     * @param acte Entity representing the Acte, used for its data.
     * @param file The main file of the acte.
     * @param annexes Annexes to the main file of the acte.
     */
    @Async
    private void sendActe(Acte acte, MultipartFile file, MultipartFile[] annexes) throws Exception {
        ActeHistory event = updateHistory(acte, StatusType.SENT_INITIATED);
        updateActeStatus(acte, event.getDate(), event.getStatus());

        String departement = "006";
        String arrondissement = "2";
        String siren = "210600730";
        String trigraph = "SIC";
        int sequence = 1;
        int deliveryNumber = new Random().nextInt(10000);

        String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"));
        String baseFilename = String.format("%s-%s-%s-%s-%s-%s-%s", 
                                    departement, 
                                    siren, 
                                    today, 
                                    acte.getNumber(),
                                    acte.getNature().getAbbreviation(),
                                    Flux.TRANSMISSION_ACTE.getTransactionNumber(),
                                    Flux.TRANSMISSION_ACTE.getFluxNumber());

        Map<Path, String> filesToArchive = new HashMap<Path, String>();
        List<String> filenamesToReference = new ArrayList<String>();

        String acteFilename = String.format("CO_DE-%s_%d.%s", baseFilename, sequence, StringUtils.getFilenameExtension(file.getOriginalFilename()));
        Path acteTempFile = createTempFile(file.getBytes());
    
        filesToArchive.put(acteTempFile, acteFilename);

        for (MultipartFile annexe : annexes) {
            sequence++;
            String tempFilename = String.format("CO_DE-%s_%d.%s", baseFilename, sequence, StringUtils.getFilenameExtension(annexe.getOriginalFilename()));
            Path tempAnnexe = createTempFile(annexe.getBytes());

            filesToArchive.put(tempAnnexe, tempFilename);
            filenamesToReference.add(tempFilename);
        }

        String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
        Message message = new Message(messageFilename);
        message.setActeFilename(acteFilename);
        message.setActeTitle(acte.getTitle());

        LocalDateTime decisionDate = LocalDateTime.ofInstant(acte.getDecision().toInstant(), ZoneId.systemDefault());
        message.setDecisionDate(decisionDate.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
        message.setNumber(acte.getNumber());
        message.setNatureCode(acte.getNature().getAbbreviation());
        message.setAnnexesFilenames(filenamesToReference);
        message.setCodeMatiere1("1");
        message.setCodeMatiere2("2");
        message.setCodeMatiere3("3");
        message.setCodeMatiere4("4");
        message.setCodeMatiere5("5");
        String messageContent = message.toXmlString();

        Path tempMessage = createTempFile(messageContent.getBytes());
        filesToArchive.put(tempMessage, messageFilename);

        String enveloppeName = String.format("EACT--%s--%s-%d.xml", siren, today, deliveryNumber);
        Enveloppe enveloppe = new Enveloppe(enveloppeName);
        enveloppe.setDepartement(departement);
        enveloppe.setArrondissement(arrondissement);
        enveloppe.setSiren(siren);
        enveloppe.setNature(acte.getNature().getAbbreviation());
        enveloppe.setContactName("Yann");
        enveloppe.setContactPhoneNumber("0000000000");
        enveloppe.setContactEmail("yann@test.fr");
        enveloppe.setCallbackEmails(Arrays.asList("mail1@test.com", "mail2@test.co.uk", "mail3@test.fr"));
        enveloppe.setMessageFilename(message.getFilename());
        String enveloppeContent = enveloppe.toXmlString();

        Path tempEnveloppe = createTempFile(enveloppeContent.getBytes());
        filesToArchive.put(tempEnveloppe, enveloppeName);

        String archiveName = String.format("%s-%s.%s",
                                           trigraph,
                                           StringUtils.stripFilenameExtension(enveloppeName),
                                           "tar.gz");

        Path tempArchive = Files.createTempFile("stela_archive_", ".tmp");
        archiveFiles(tempArchive.toFile(), filesToArchive);
        Path archive = Files.move(tempArchive, tempArchive.resolveSibling(archiveName));
        LOGGER.info("Archive created : {}", archive.toString());
    }

    private Path createTempFile(byte[] source) throws IOException {
        Path tmpFile = Files.createTempFile("stela_acte_", ".tmp");
        return Files.write(tmpFile, source);
    }

    private void archiveFiles(File archive, Map<Path, String> filesToArchive) throws Exception {
            try (
                FileOutputStream fOut = new FileOutputStream(archive);
                BufferedOutputStream bOut = new BufferedOutputStream(fOut);
                CompressorOutputStream gzOut = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, bOut);
                TarArchiveOutputStream archiver = new TarArchiveOutputStream(gzOut);
            ) {
                for (Map.Entry<Path, String> entry : filesToArchive.entrySet()) {
                    File file = entry.getKey().toFile();
                    TarArchiveEntry tarEntry = new TarArchiveEntry(file, entry.getValue());
                    archiver.putArchiveEntry(tarEntry);
                    IOUtils.copy(new FileInputStream(file), archiver);
                    archiver.closeArchiveEntry();
                }

                archiver.finish();
            }
    }
}
