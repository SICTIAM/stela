package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.PesAllerRepository;
import fr.sictiam.stela.pesservice.dao.PesRetourRepository;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.NotificationService;
import fr.sictiam.stela.pesservice.service.PesAllerService;
import fr.sictiam.stela.pesservice.service.StorageService;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = ReceiverTask.class,
        properties = {"application.receiverTask.hoursWithoutNewFiles = 4",
                "application.receiverTask.maxWaitingPes = 50",
                "application.receiverTask.alertEmail = dev@sictiam.fr",
                "application.ftp.timeout = 60"
        })
public class ReceiverTaskTest {

    @Autowired
    private ReceiverTask receiverTask;

    @MockBean
    private PesAllerService pesAllerService;

    @MockBean
    private PesRetourRepository pesRetourRepository;

    @MockBean
    private PesAllerRepository pesAllerRepository;

    @MockBean
    private LocalAuthorityService localAuthorityService;

    @MockBean
    private DefaultFtpSessionFactory defaultFtpSessionFactory;

    @MockBean
    private StorageService storageService;

    @MockBean
    private NotificationService notificationService;


    @Test
    public void testAckKoWithInnerDetailLigne() throws Exception {

        PesAller pesAller = gimmePesAller();
        given(pesAllerService.getByFileName("PesDepense-02-2019-5-11112222333344445"))
                .willReturn(Optional.of(pesAller));
        doNothing().when(pesAllerService).updateStatus(anyString(), any(), any(), anyString(), anyList());

        InputStream pesFile = new ClassPathResource("data/pes-ack-ko/006111_190130123456-ACK-A3020100_A0000IDL_KO.xml")
                .getInputStream();
        receiverTask.readACK(IOUtils.toByteArray(pesFile), "ackName");

        verify(pesAllerService).updateStatus(eq(pesAller.getUuid()), eq(StatusType.NACK_RECEIVED),
                any(), eq("ackName"), argThat(errors ->
                        errors.size() == 6 &&
                                errors.stream().allMatch(pesHistoryError -> pesHistoryError.getTitle().equals("ERREUR_AUTRE")) &&
                                errors.stream().filter(pesHistoryError -> pesHistoryError.getMessage().startsWith("1971")).count() == 5 &&
                                errors.stream().noneMatch(pesHistoryError -> pesHistoryError.getSource().isEmpty())));
    }

    @Test
    public void testAckKoWithDetailPiece() throws Exception {

        PesAller pesAller = gimmePesAller();
        given(pesAllerService.getByFileName("PesRecette-02-2018-258-258-20190129666666"))
                .willReturn(Optional.of(pesAller));
        doNothing().when(pesAllerService).updateStatus(anyString(), any(), any(), anyString(), anyList());

        InputStream pesFile = new ClassPathResource("data/pes-ack-ko/006111_190130123456-ACK-A3020100_A0000SDP_KO.xml")
                .getInputStream();
        receiverTask.readACK(IOUtils.toByteArray(pesFile), "ackName");

        verify(pesAllerService).updateStatus(eq(pesAller.getUuid()), eq(StatusType.NACK_RECEIVED),
                any(), eq("ackName"), argThat(errors ->
                        errors.size() == 2 &&
                                errors.stream().allMatch(pesHistoryError -> pesHistoryError.getTitle().equals("ERREUR_AUTRE")) &&
                                errors.stream().filter(pesHistoryError -> pesHistoryError.getMessage().startsWith("2559")).count() == 2 &&
                                errors.stream().noneMatch(pesHistoryError -> pesHistoryError.getSource().isEmpty())));
    }

    @Test
    public void testAckKoWithSimpleErreur() throws Exception {

        PesAller pesAller = gimmePesAller();
        given(pesAllerService.getByFileName("PesDepense-12-2018-209-2092019012917666666"))
                .willReturn(Optional.of(pesAller));
        doNothing().when(pesAllerService).updateStatus(anyString(), any(), any(), anyString(), anyList());

        InputStream pesFile = new ClassPathResource("data/pes-ack-ko/006111_190130123456-ACK-A3020100_A0000SEN_KO.xml")
                .getInputStream();
        receiverTask.readACK(IOUtils.toByteArray(pesFile), "ackName");

        verify(pesAllerService).updateStatus(eq(pesAller.getUuid()), eq(StatusType.NACK_RECEIVED),
                any(), eq("ackName"), argThat(errors ->
                        errors.size() == 1 &&
                                errors.stream().allMatch(pesHistoryError -> pesHistoryError.getTitle().equals("ERREUR_DOUBLON_PJ")
                                        && pesHistoryError.getSource().equals("TI00064567")
                                        && pesHistoryError.getMessage().startsWith("1984"))));
    }

    @Test
    public void testAckKoWithAMixOfErreurPatterns() throws Exception {

        PesAller pesAller = gimmePesAller();
        given(pesAllerService.getByFileName("PesDepense-02-2019-5-11112222333355556"))
                .willReturn(Optional.of(pesAller));
        doNothing().when(pesAllerService).updateStatus(anyString(), any(), any(), anyString(), anyList());

        InputStream pesFile = new ClassPathResource("data/pes-ack-ko/006111_190130123456-ACK-A3020100_A0000MIX_KO.xml")
                .getInputStream();
        receiverTask.readACK(IOUtils.toByteArray(pesFile), "ackName");

        verify(pesAllerService).updateStatus(eq(pesAller.getUuid()), eq(StatusType.NACK_RECEIVED),
                any(), eq("ackName"), argThat(errors ->
                        errors.size() == 6 &&
                                errors.stream().noneMatch(pesHistoryError -> pesHistoryError.getTitle().isEmpty()) &&
                                errors.stream().noneMatch(pesHistoryError -> pesHistoryError.getMessage().isEmpty()) &&
                                errors.stream().noneMatch(pesHistoryError -> pesHistoryError.getSource().isEmpty())));
    }

    private PesAller gimmePesAller() {
        return new PesAller("uuid", LocalDateTime.now(), "objet", "filetype", LocalDateTime.now(), StatusType.SENT);
    }
}
