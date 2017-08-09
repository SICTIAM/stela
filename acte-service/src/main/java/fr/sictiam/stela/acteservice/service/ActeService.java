package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.controller.ActeNotFoundException;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.StatusType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
        acte.setCreation(LocalDateTime.now());
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

    void updateStatus(Acte acte, LocalDateTime date, StatusType status, String message) {
        acteHistoryRepository.save(new ActeHistory(acte.getUuid(), status, date, message));
        acte.setStatus(status);
        acte.setLastUpdateTime(date);
        acteRepository.save(acte);
    }

    public List<ActeHistory> getHistory(String uuid) {
        return acteHistoryRepository.findByActeUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

}
