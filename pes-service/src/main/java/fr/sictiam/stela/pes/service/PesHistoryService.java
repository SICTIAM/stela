package fr.sictiam.stela.pes.service;

import fr.sictiam.stela.pes.dao.PesHistoryRepository;
import fr.sictiam.stela.pes.model.PesHistory;
import org.springframework.stereotype.Service;

@Service
public class PesHistoryService {

    private final PesHistoryRepository pesHistoryRepository;

    public PesHistoryService(PesHistoryRepository pesHistoryRepository) {
        this.pesHistoryRepository = pesHistoryRepository;
    }

    public PesHistory newPesHistory(PesHistory pesHistory) {
        return pesHistoryRepository.save(pesHistory);
    }
}
