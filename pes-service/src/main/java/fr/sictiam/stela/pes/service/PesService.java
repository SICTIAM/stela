package fr.sictiam.stela.pes.service;

import fr.sictiam.stela.pes.dao.PesRepository;
import fr.sictiam.stela.pes.model.Pes;
import fr.sictiam.stela.pes.model.event.PesCreatedEvent;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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
    private PesEventService pesEventService;

    private final PesRepository pesRepository;

    public PesService(PesRepository pesRepository) {
        this.pesRepository = pesRepository;
    }

    public void create(Pes pes) {
        // TODO: Controls/validation before creation, return either pes or an error string
        pesRepository.save(pes);
        PesCreatedEvent pesCreatedEvent = pesEventService.createdEvent(pes);
        amqpTemplate.convertAndSend(exchange, createdKey, pesCreatedEvent);
    }

    public List<Pes> getAll() {
        return pesRepository.findAll();
    }

}
