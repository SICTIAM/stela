package fr.sictiam.stela.admin.service;

import fr.sictiam.stela.admin.dao.PaullConnectionRepository;
import fr.sictiam.stela.admin.model.PaullConnection;
import fr.sictiam.stela.admin.service.exceptions.SessionNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class PaullConnectionService {

    private final PaullConnectionRepository paullConnectionRepository;

    public PaullConnectionService(PaullConnectionRepository paullConnectionRepository) {
        this.paullConnectionRepository = paullConnectionRepository;
    }

    public PaullConnection save(PaullConnection paullConnection) {
        return paullConnectionRepository.save(paullConnection);
    }

    public PaullConnection getBySessionID(String sessionID) {
        return paullConnectionRepository.findById(sessionID).orElseThrow(SessionNotFoundException::new);
    }
}
