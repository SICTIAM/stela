package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PendingMessageRepository;
import fr.sictiam.stela.pesservice.model.PendingMessage;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

import java.util.List;

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
