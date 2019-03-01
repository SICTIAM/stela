package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import fr.sictiam.stela.pesservice.model.StatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricService {

    private final PesHistoryRepository pesHistoryRepository;

    @Autowired
    public MetricService(PesHistoryRepository pesHistoryRepository) {
        this.pesHistoryRepository = pesHistoryRepository;
    }

    public Map<String, Long> getNumberOfPes(LocalDateTime fromLocalDate, LocalDateTime toLocalDate, StatusType statusType) {
        Map<String, Long> listNumberPesByType = new HashMap<>();

        if(statusType == null) {
            for (StatusType type : StatusType.values()) {
                listNumberPesByType.put(
                        type.name(),
                        pesHistoryRepository.countByDateBetweenAndStatus(fromLocalDate, toLocalDate, type));
            }
        } else {
            listNumberPesByType.put(
                    statusType.name(),
                    pesHistoryRepository.countByDateBetweenAndStatus(fromLocalDate, toLocalDate, statusType));
        }
        return listNumberPesByType;
    }

    public Map<String, List<Map<LocalDateTime, Long>>> getNumberOfPesWithSample(LocalDateTime fromLocalDate, LocalDateTime toLocalDate, StatusType statusType, String sample) {
        Map<String, List<Map<LocalDateTime, Long>>> listNumberPesByType = new HashMap<>();

        if(statusType == null) {
            for (StatusType type : StatusType.values()) {
                listNumberPesByType.put(
                        type.name(),
                        pesHistoryRepository.countByDateBetweenAndGroupBySample(sample, type.name(), fromLocalDate, toLocalDate));
            }
        } else {
            listNumberPesByType.put(
                    statusType.name(),
                    pesHistoryRepository.countByDateBetweenAndGroupBySample(sample, statusType.name(), fromLocalDate, toLocalDate));
        }
        return listNumberPesByType;
    }
}
