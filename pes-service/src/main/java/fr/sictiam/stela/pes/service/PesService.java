package fr.sictiam.stela.pes.service;

import fr.sictiam.stela.pes.dao.PesRepository;
import fr.sictiam.stela.pes.model.Pes;
import fr.sictiam.stela.pes.model.PesHistory;
import fr.sictiam.stela.pes.model.StatusType;
import fr.sictiam.stela.pes.model.event.PesCreatedEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class PesService {

    @Value("${application.amqp.pes.createdKey}")
    private String createdKey;

    @Value("${application.amqp.pes.exchange}")
    private String exchange;

    @Autowired
    private AmqpTemplate amqpTemplate;

    @Autowired
    private PesHistoryService pesHistoryService;

    @Autowired
    private PesRepository pesRepository;

    public void create(Pes pes) {
        // TODO: Controls/validation before creation, return either pes or an error string
        pes = pesRepository.save(pes);
        PesCreatedEvent pesCreatedEvent = new PesCreatedEvent(pes, "pes-service", new Date());
        PesHistory pesHistory = pesHistoryService.create(new PesHistory(pes.getUuid(), StatusType.CREATED, "pes-service", new Date()));
        pes.setCreationDate(pesHistory.getDate());
        updatePesLastHistory(pes, pesHistory.getDate(), pesHistory.getStatus());
        amqpTemplate.convertAndSend(exchange, createdKey, pesCreatedEvent);
    }

    public List<Pes> getAll() {
        return pesRepository.findAll();
    }

    public Pes getByUuid(String uuid) {
        return pesRepository.findByUuid(uuid);
    }

    public void updatePesLastHistory(Pes pes, Date date, StatusType status) {
        pes.setStatus(status);
        pes.setLastUpdateTime(date);
        pesRepository.save(pes);
    }

}
