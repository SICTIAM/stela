package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.controller.ActeNotFoundException;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.*;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.CompressorOutputStream;
import org.apache.commons.compress.compressors.CompressorStreamFactory;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ActeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeService.class);
    
    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;

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
    public Acte create(Acte acte, MultipartFile file, MultipartFile[] annexes)
            throws ActeNotSentException, IOException {
        acte.setFilename(file.getOriginalFilename());
        acte.setFile(file.getBytes());
        List<Attachment> transformedAnnexes = new ArrayList<>();
        for (MultipartFile annexe: annexes) {
            transformedAnnexes.add(new Attachment(annexe.getBytes(), annexe.getOriginalFilename()));
        }
        acte.setAnnexes(transformedAnnexes);
        acte.setCreation(new Date());
        acte.setStatus(StatusType.CREATED);

        Acte created = acteRepository.save(acte);
        acteHistoryRepository.save(new ActeHistory(acte.getUuid(), StatusType.CREATED, acte.getCreation(), null));

        LOGGER.info("Acte {} created with id {}", created.getNumber(), created.getUuid());

        return created;
    }

    public List<Acte> getAll() {
        return acteRepository.findAllByOrderByCreationDesc();
    }

    public Acte getByUuid(String uuid) {
        return acteRepository.findByUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

    public List<Attachment> getAnnexes(String acteUuid) {
        return getByUuid(acteUuid).getAnnexes();
    }

    private void updateStatus(Acte acte, Date date, StatusType status, String message) {
        acteHistoryRepository.save(new ActeHistory(acte.getUuid(), status, new Date(), message));
        acte.setStatus(status);
        acte.setLastUpdateTime(date);
        acteRepository.save(acte);
    }

    public List<ActeHistory> getActeHistory(String uuid) {
        return acteHistoryRepository.findByActeUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

    /**
     * Compress file and annexes into a tar.gz archive and delivers it to minister.
     */
    @Scheduled(fixedRate = 1000)
    private void sendActe() throws Exception {
        acteRepository.findByStatus(StatusType.CREATED).forEach(acte -> {
            try {
                updateStatus(acte, new Date(), StatusType.SENT_INITIATED, null);

                String departement = "006";
                String arrondissement = "2";
                String siren = "210600730";
                String trigraph = "SIC";
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

                Map<String, byte[]> filesToArchive = new HashMap<>();
                List<String> filenamesToReference = new ArrayList<>();

                String acteFilename =
                        String.format("CO_DE-%s_%d.%s", baseFilename, 1, StringUtils.getFilenameExtension(acte.getFilename()));
                filesToArchive.put(acteFilename, acte.getFile());

                acte.getAnnexes().forEach( attachment -> {
                    int sequence = filesToArchive.size() + 1;
                    String tempFilename =
                            String.format("CO_DE-%s_%d.%s", baseFilename, sequence,
                                    StringUtils.getFilenameExtension(attachment.getFilename()));
                    filesToArchive.put(tempFilename, attachment.getFile());
                    filenamesToReference.add(tempFilename);
                });

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

                filesToArchive.put(messageFilename, messageContent.getBytes());

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

                filesToArchive.put(enveloppeName, enveloppeContent.getBytes());

                String archiveName = String.format("%s-%s.%s",
                        trigraph,
                        StringUtils.stripFilenameExtension(enveloppeName),
                        "tar.gz");

                Path tempArchive = Files.createTempFile("stela_archive_", ".tmp");
                archiveFiles(tempArchive.toFile(), filesToArchive);
                Path archive = Files.move(tempArchive, tempArchive.resolveSibling(archiveName));
                LOGGER.info("Archive created : {}", archive.toString());
            } catch (Exception e) {
                LOGGER.error("Error while generating envelop for acte {}", acte.getNumber());
                updateStatus(acte, new Date(), StatusType.FILE_ERROR, e.getMessage());
            }
        });
    }

    private void archiveFiles(File archive, Map<String, byte[]> filesToArchive) throws Exception {
        try (
                FileOutputStream fOut = new FileOutputStream(archive);
                BufferedOutputStream bOut = new BufferedOutputStream(fOut);
                CompressorOutputStream gzOut = new CompressorStreamFactory().createCompressorOutputStream(CompressorStreamFactory.GZIP, bOut);
                TarArchiveOutputStream archiver = new TarArchiveOutputStream(gzOut)
        ) {
            for (Map.Entry<String, byte[]> entry : filesToArchive.entrySet()) {
                File file = new File(entry.getKey());
                TarArchiveEntry tarEntry = new TarArchiveEntry(file, entry.getKey());
                archiver.putArchiveEntry(tarEntry);
                IOUtils.copy(new FileInputStream(file), archiver);
                archiver.closeArchiveEntry();
            }

            archiver.finish();
        }
    }
}
