package com.sictiam.flux_pes.service;

import com.sictiam.flux_pes.model.PesObjet;
import com.sictiam.flux_pes.repository.PesObjRepositery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
public class PesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PesService.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;
    private PesObjRepositery repopes;
    public void sendTestMessage(String message) {
        LOGGER.warn("Sending test message to Kafka");
        kafkaTemplate.send("test-topic", message);
    }

    public void storepes(PesObjet pesobj) {
        pesobj.toString();
        repopes.findAll();
    }

    @KafkaListener(topics = "test-topic")
    public void receiveTestMessage(String message) {

        LOGGER.warn("Got a message : {}", message);
        PesObjet objpes;
        objpes = new PesObjet(1,message);
        LOGGER.warn("Objet pes à créer : {}", objpes);
        storepes(objpes);
        //PesObjRepositery repopes;
        //sauvegarde en base de données
        //repopes.save(objpes);
        //repopes.findAll();
    }
}
