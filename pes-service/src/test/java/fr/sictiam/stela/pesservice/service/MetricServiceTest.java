package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.PesHistoryRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import javax.persistence.EntityManager;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = MetricService.class)
public class MetricServiceTest {

    @Autowired
    private MetricService metricService;

    @MockBean
    private PesHistoryRepository pesHistoryRepository;

    @MockBean
    private EntityManager entityManager;

    @Test
    public void getNumberOfPes() {

    }
}