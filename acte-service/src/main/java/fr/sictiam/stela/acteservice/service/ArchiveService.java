package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.Flux;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.xml.Enveloppe;
import fr.sictiam.stela.acteservice.model.xml.Message;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.StringUtils;

import java.io.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class ArchiveService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ArchiveService.class);

    private final ActeRepository acteRepository;
    private final ActeService acteService;

    public ArchiveService(ActeRepository acteRepository, ActeService acteService) {
        this.acteRepository = acteRepository;
        this.acteService = acteService;
    }

    /**
     * Compress file and annexes into a tar.gz archive and delivers it to minister.
     */
    @Scheduled(fixedRate = 1000)
    public void createArchive() throws Exception {
        acteRepository.findByStatus(StatusType.CREATED).forEach(acte -> {
            try {
                String departement = "006";
                String arrondissement = "2";
                String siren = "210600730";
                String trigraph = "SIC";
                int deliveryNumber = new Random().nextInt(10000);

                String today = LocalDateTime.now().format(DateTimeFormatter.ofPattern("YYYYMMdd"));

                // this is the base filename for the message and attachments
                String baseFilename = String.format("%s-%s-%s-%s-%s-%s-%s",
                        departement,
                        siren,
                        today,
                        acte.getNumber(),
                        acte.getNature().getAbbreviation(),
                        Flux.TRANSMISSION_ACTE.getTransactionNumber(),
                        Flux.TRANSMISSION_ACTE.getFluxNumber());

                Map<String, byte[]> annexes = new HashMap<>();

                String acteFilename =
                        String.format("CO_DE-%s_%d.%s", baseFilename, 1, StringUtils.getFilenameExtension(acte.getFilename()));

                acte.getAnnexes().forEach(attachment -> {
                    // sequence 1 is taken by the Acte file, so we start at two
                    int sequence = annexes.size() + 2;
                    String tempFilename =
                            String.format("CO_DE-%s_%d.%s", baseFilename, sequence,
                                    StringUtils.getFilenameExtension(attachment.getFilename()));
                    annexes.put(tempFilename, attachment.getFile());
                });

                String messageFilename = String.format("%s_%d.xml", baseFilename, 0);
                Message message = new Message(messageFilename);
                message.setActeFilename(acteFilename);
                message.setActeTitle(acte.getTitle());

                LocalDateTime decisionDate = LocalDateTime.ofInstant(acte.getDecision().toInstant(), ZoneId.systemDefault());
                message.setDecisionDate(decisionDate.format(DateTimeFormatter.ofPattern("YYYY-MM-dd")));
                message.setNumber(acte.getNumber());
                message.setNatureCode(acte.getNature().getAbbreviation());
                message.setAnnexesFilenames(new ArrayList<>(annexes.keySet()));
                message.setCodeMatiere1("1");
                message.setCodeMatiere2("2");
                message.setCodeMatiere3("3");
                message.setCodeMatiere4("4");
                message.setCodeMatiere5("5");
                String messageContent = message.toXmlString();

                String enveloppeName = String.format("EACT--%s--%s-%d.xml", siren, today, deliveryNumber);
                // TODO : hardcoded variables
                Enveloppe enveloppe = new Enveloppe(enveloppeName, siren, departement, arrondissement,
                        acte.getNature().getAbbreviation(), "SICTIAM", "0101010101",
                        "dev@sictiam.fr", Collections.singletonList("dev@sictiam.fr"), message.getFilename());
                String enveloppeContent = enveloppe.toXmlString();

                String archiveName = String.format("%s-%s.%s",
                        trigraph,
                        StringUtils.stripFilenameExtension(enveloppeName),
                        "tar.gz");

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                TarArchiveOutputStream taos = new TarArchiveOutputStream(baos);

                addEntry(enveloppeName, enveloppeContent.getBytes(), taos);
                addEntry(messageFilename, messageContent.getBytes(), taos);
                addEntry(acteFilename, acte.getFile(), taos);
                for (String annexeName: annexes.keySet()) {
                    addEntry(annexeName, annexes.get(annexeName), taos);
                }

                taos.close();
                baos.close();

                ByteArrayOutputStream baos2 = compress(baos);

                byte[] archiveData = baos2.toByteArray();
                acte.setArchive(archiveData);
                acte.setArchiveName(archiveName);
                acteRepository.save(acte);

                acteService.updateStatus(acte, new Date(), StatusType.ARCHIVE_CREATED, null);

                LOGGER.info("Archive created : {}", archiveName);
            } catch (Exception e) {
                LOGGER.error("Error while generating archive for acte {} : {}", acte.getNumber(), e.getMessage());
                acteService.updateStatus(acte, new Date(), StatusType.FILE_ERROR, e.getMessage());
            }
        });
    }

    private void addEntry(String entryName, byte[] content, TarArchiveOutputStream taos) throws IOException {
        File file = new File(entryName);
        FileCopyUtils.copy(content, file);
        ArchiveEntry archiveEntry = new TarArchiveEntry(file, entryName);
        taos.putArchiveEntry(archiveEntry);
        IOUtils.copy(new FileInputStream(file), taos);
        taos.closeArchiveEntry();
        file.delete();
    }

    private ByteArrayOutputStream compress(ByteArrayOutputStream baos) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        GzipCompressorOutputStream gcos = new GzipCompressorOutputStream(baos2);
        final byte[] buffer = new byte[2048];
        int n;
        while (-1 != (n = bais.read(buffer))) {
            gcos.write(buffer, 0, n);
        }
        gcos.close();
        bais.close();
        return baos2;
    }
}
