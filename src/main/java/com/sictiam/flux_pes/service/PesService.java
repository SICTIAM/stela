package com.sictiam.flux_pes.service;

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

    public void sendTestMessage(String message) {
        LOGGER.warn("Sending test message to Kafka");
        kafkaTemplate.send("test-topic", message);
    }

    @KafkaListener(topics = "test-topic")
    public void receiveTestMessage(String message) {
        LOGGER.warn("Got a message : ", message);
    }
}
