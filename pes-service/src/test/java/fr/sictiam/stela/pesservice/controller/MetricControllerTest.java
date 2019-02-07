package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.MetricService;
import fr.sictiam.stela.pesservice.service.util.CertUtilService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManagerFactory;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(MetricController.class)
public class MetricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MetricService metricService;

    @MockBean
    private PesAllerRepository pesAllerRepository;

    @MockBean
    EntityManagerFactory entityManagerFactory;

    @MockBean
    CertUtilService certUtilService;

    @Test
    public void getNumberOfPesWithoutType() throws Exception {
        LocalDateTime localDateTimeNow = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String formatedDateTime = localDateTimeNow.format(formatter);

        Map<String, Long> listPesNumberByType = new HashMap<>();
        for(StatusType statusType: StatusType.values()) {
            listPesNumberByType.put(statusType.name(), (long) 0);
        }

        System.out.printf("test %s", listPesNumberByType.size());

        given(metricService.getNumberOfPes(any(), any(), eq(null))).willReturn(listPesNumberByType);

        this.mockMvc.perform(get("/api/pes/metric")
                .param("fromDate", formatedDateTime)
                .param("toDate", formatedDateTime))
                .andExpect(status().isOk());

        verify(metricService, times(1)).getNumberOfPes(any(), any(),eq(null));
        verifyNoMoreInteractions(metricService);
    }
}