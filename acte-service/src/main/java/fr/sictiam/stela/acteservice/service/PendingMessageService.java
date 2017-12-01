package fr.sictiam.stela.acteservice.service;

import java.util.List;

import javax.transaction.Transactional;

import org.springframework.stereotype.Service;

import fr.sictiam.stela.acteservice.dao.PendingMessageRepository;
import fr.sictiam.stela.acteservice.model.PendingMessage;

@Service
public class PendingMessageService {

    private final PendingMessageRepository pendingMessageRepository;

    public PendingMessageService(PendingMessageRepository pendingMessageRepository) {
        this.pendingMessageRepository = pendingMessageRepository;
    }

    @Transactional
    public PendingMessage save(PendingMessage pendingMessage) {
       return pendingMessageRepository.save(pendingMessage);
    }

    @Transactional
    public void remove(PendingMessage pendingMessage) {
        pendingMessageRepository.delete(pendingMessage);
    }
    
    @Transactional
    public List<PendingMessage> getAllPendingMessages() {
        return pendingMessageRepository.findAll();
    }
}
