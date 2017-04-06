package com.sictiam.flux_pes.service;

import com.sictiam.flux_pes.command.AddAcknowledgmentOfReceipCommand;
import com.sictiam.flux_pes.command.CreatePesCommand;
import com.sictiam.flux_pes.model.Pes;
import com.sictiam.flux_pes.model.PesAggregate;
import com.sictiam.flux_pes.model.PesObjet;
import com.sictiam.flux_pes.repository.PesRepo1;
import com.sictiam.flux_pes.repository.PesRepository;
import org.axonframework.commandhandling.CommandHandler;
import org.axonframework.eventsourcing.EventSourcingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
//import org.json.*;
//import com.google.gson.Gson;

import java.util.UUID;

@Service
public class PesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesService.class);
    /*private EventSourcingRepository<PesAggregate> pesRepository;
    private PesRepository pesRepo1;
    private PesRepo1 pesRepo2; */

    @Autowired
    private KafkaTemplate kafkaTemplate;
    //@Autowired
    //private PesRepository pesRepository;
    //@Autowired
    private EventSourcingRepository<Pes> peseventsourcingrepo;
    //private EventSourcingRepository<PesAggregate> peseventsourcingrepo;

    @CommandHandler
    public void create(CreatePesCommand createPesCommand) {
        Pes pes = new Pes(UUID.randomUUID().toString(), createPesCommand.getTitre());

        LOGGER.warn("Sending test message to Kafka {}", pes.toString());
        kafkaTemplate.send("test-topic", pes);
    }

    @CommandHandler
    public void addAcknowledgmentOfReceipt(AddAcknowledgmentOfReceipCommand addAcknowledgmentOfReceipCommand) {

    }

    @KafkaListener(topics = "test-topic")
    public void receiveTestMessage(Pes pes)  {

        LOGGER.warn("Got a PES flow : {} ", pes);
        LOGGER.warn("sauver données PES : {} ", pes);
        //PesAggregate pesaggregate = new PesAggregate(pes.getId(), pes.getTitre());
        /*Pes pes2 = new Pes("6", "titre");
        pesRepository.save(pes2); */
        try {
            peseventsourcingrepo.newInstance(() -> new Pes(pes.getId(), pes.getTitre()));
        }
        catch(Exception e)
        {
            LOGGER.warn("Pb enreg event sourcing : {} ", e.getMessage());
        }
    }
}