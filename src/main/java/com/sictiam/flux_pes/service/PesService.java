package com.sictiam.flux_pes.service;

import com.sictiam.flux_pes.command.AddAcknowledgmentOfReceipCommand;
import com.sictiam.flux_pes.command.CreatePesCommand;
import com.sictiam.flux_pes.model.Pes;
import com.sictiam.flux_pes.repository.PesRepository;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.commandhandling.model.AggregateLifecycle;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesService.class);

    private EventSourcingRepository<Pes> pesRepository;

    @Autowired
    private KafkaTemplate kafkaTemplate;

    @CommandHandler
    public void create(CreatePesCommand createPesCommand) {
        Pes pes = new Pes(UUID.randomUUID().toString(), createPesCommand.getTitre());

        LOGGER.warn("Sending test message to Kafka");
        kafkaTemplate.send("test-topic", pes);
    }

    @CommandHandler
    public void addAcknowledgmentOfReceipt(AddAcknowledgmentOfReceipCommand addAcknowledgmentOfReceipCommand) {

    }

    @KafkaListener(topics = "test-topic")
    public void receiveTestMessage(Pes pes) throws Exception {

        LOGGER.warn("Got a PES flow : {} ({})", pes.getId(), pes.getTitre());
        pesRepository.newInstance(() -> pes);
    }
}
