package fr.sictiam.stela.pes.service;

import fr.sictiam.stela.pes.dao.PesEventRepository;
import fr.sictiam.stela.pes.model.Pes;
import fr.sictiam.stela.pes.model.event.PesCreatedEvent;
import fr.sictiam.stela.pes.model.event.PesEvent;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PesEventService {

    private final PesEventRepository pesEventRepository;

    public PesEventService(PesEventRepository pesEventRepository) {
        this.pesEventRepository = pesEventRepository;
    }

    public List<PesEvent> getEvents() {
        return pesEventRepository.findAll();
    }

    public void save(PesEvent pesEvent) {
        pesEventRepository.save(pesEvent);
    }

    public List<PesEvent> getEventsById(String pesId) {
        return pesEventRepository.findAllByPesId(pesId);
    }

    public PesCreatedEvent createdEvent(Pes pes) {
        PesCreatedEvent pesCreatedEvent = new PesCreatedEvent(pes.getPesId(),
                pes.getTitle(),
                pes.getFileContent(),
                pes.getFileName(),
                pes.getComment(),
                pes.getGroupId(),
                pes.getUserId());
        return pesEventRepository.save(pesCreatedEvent);
    }
}
