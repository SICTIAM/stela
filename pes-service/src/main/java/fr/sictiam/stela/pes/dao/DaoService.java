package fr.sictiam.stela.pes.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DaoService {

    @Autowired
    private PesRepository pesRepository;

    @Autowired
    private PesHistoryRepository pesHistoryRepository;

    public void cleanDb() {
        pesRepository.deleteAll();
        pesHistoryRepository.deleteAll();
    }
}
