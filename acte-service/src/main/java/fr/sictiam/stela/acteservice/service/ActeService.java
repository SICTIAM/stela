package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.controller.ActeNotFoundException;
import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActeService implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActeService.class);
    
    private final ActeRepository acteRepository;
    private final ActeHistoryRepository acteHistoryRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    public ActeService(ActeRepository acteRepository, ActeHistoryRepository acteHistoryRepository, ApplicationEventPublisher applicationEventPublisher) {
        this.acteRepository = acteRepository;
        this.acteHistoryRepository = acteHistoryRepository;
        this.applicationEventPublisher = applicationEventPublisher;
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
    public Acte create(Acte acte, MultipartFile file, MultipartFile... annexes)
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

        ActeHistory acteHistory = new ActeHistory(acte.getUuid(), StatusType.CREATED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));

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

    public List<ActeHistory> getHistory(String uuid) {
        return acteHistoryRepository.findByActeUuid(uuid).orElseThrow(ActeNotFoundException::new);
    }

    public void cancel(String uuid) {
        ActeHistory acteHistory = new ActeHistory(uuid, StatusType.CANCELLATION_ASKED);
        applicationEventPublisher.publishEvent(new ActeHistoryEvent(this, acteHistory));
    }

    public boolean isCancellable(String uuid) {
        Acte acte = getByUuid(uuid);
        return acte.getStatus().equals(StatusType.ACK_RECEIVED);
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        acteHistoryRepository.save(event.getActeHistory());
        Acte acte = getByUuid(event.getActeHistory().getActeUuid());
        acte.setStatus(event.getActeHistory().getStatus());
        acte.setLastUpdateTime(event.getActeHistory().getDate());
        acteRepository.save(acte);
    }
}
