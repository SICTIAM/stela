package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.model.StatusType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class MetricService {

    private final PesAllerRepository pesAllerRepository;

    @Autowired
    public MetricService(PesAllerRepository pesAllerRepository) {
        this.pesAllerRepository = pesAllerRepository;
    }

    public Map<String, Long> getNumberOfPes(LocalDateTime fromLocalDate, LocalDateTime toLocalDate, StatusType statusType) {
        Map<String, Long> listNumberPesByType = new HashMap<>();

        if(statusType == null) {
            for (StatusType type : StatusType.values()) {
                listNumberPesByType.put(
                        type.name(),
                        pesAllerRepository.countByCreationBetweenAndLastHistoryStatus(fromLocalDate, toLocalDate, type));
            }
        } else {
            listNumberPesByType.put(
                    statusType.name(),
                    pesAllerRepository.countByCreationBetweenAndLastHistoryStatus(fromLocalDate, toLocalDate, statusType));
        }
        return listNumberPesByType;
    }

    public Map<String, List<Map<LocalDateTime, Long>>> getNumberOfPesWithSample(LocalDateTime fromLocalDate, LocalDateTime toLocalDate, StatusType statusType, String sample) {
        Map<String, List<Map<LocalDateTime, Long>>> listNumberPesByType = new HashMap<>();

        if(statusType == null) {
            for (StatusType type : StatusType.values()) {
                listNumberPesByType.put(
                        type.name(),
                        pesAllerRepository.countByCreationBetweenAndGroupByDay(sample, type.name(), fromLocalDate, toLocalDate));
            }
        } else {
            listNumberPesByType.put(
                    statusType.name(),
                    pesAllerRepository.countByCreationBetweenAndGroupByDay(sample, statusType.name(), fromLocalDate, toLocalDate));
        }
        return listNumberPesByType;
    }
}
