package fr.sictiam.stela.pesservice.controller;


import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.MetricService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/pes/metric")
public class MetricController {

    private final MetricService metricService;

    public MetricController(MetricService metricService) {
        this.metricService = metricService;
    }

    @GetMapping
    public Map<String, Long> getNumberOfPes(
            @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fromDate,
            @RequestParam(value = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime toDate,
            @RequestParam(value = "statusType", required = false) StatusType statusType) {
        return metricService.getNumberOfPes(fromDate, toDate, statusType);
    }

    @GetMapping("/sample")
    public Map<String, List<Map<LocalDateTime, Long>>> getNumberOfPesWithSample(
            @RequestParam(value = "fromDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime fromDate,
            @RequestParam(value = "toDate") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime toDate,
            @RequestParam(value = "statusType", required = false) StatusType statusType,
            @RequestParam(value = "sample", required = false) String sample) {
        return metricService.getNumberOfPesWithSample(fromDate, toDate, statusType, sample);
    }
}
