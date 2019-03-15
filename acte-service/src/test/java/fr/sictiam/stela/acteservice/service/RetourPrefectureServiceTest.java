package fr.sictiam.stela.acteservice.service;

import fr.sictiam.stela.acteservice.dao.ActeHistoryRepository;
import fr.sictiam.stela.acteservice.dao.ActeRepository;
import fr.sictiam.stela.acteservice.dao.LocalAuthorityRepository;
import fr.sictiam.stela.acteservice.model.*;
import fr.sictiam.stela.acteservice.model.xml.*;
import fr.sictiam.stela.acteservice.service.util.XmlUtils;
import fr.sictiam.stela.acteservice.service.util.ZipGeneratorUtil;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringRunner;

import javax.xml.bind.JAXBException;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static fr.sictiam.stela.acteservice.TestDataGenerator.acte;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = RetourPrefectureService.class)
public class RetourPrefectureServiceTest {

    /*
     * Due to https://github.com/spring-projects/spring-framework/issues/18907, history interactions are checked
     * at the #RetourPrefectureService.publishActeHistory method level, not at the publishEvent one.
     */
    @SpyBean
    private RetourPrefectureService retourPrefectureService;

    @MockBean
    private ActeRepository acteRepository;
    @MockBean
    private ActeHistoryRepository acteHistoryRepository;
    @MockBean
    private LocalAuthorityRepository localAuthorityRepository;
    @MockBean
    private ZipGeneratorUtil zipGeneratorUtil;

    @Test
    public void testReceiveARActe() throws IOException, JAXBException {

        given(acteRepository.findByMiatId(anyString()))
                .willReturn(Optional.of(acte()));

        Pair<ARActe, Attachment> parsedData =
                parseClassPathResource("/data/006-210600235-20180522-684-AI-1-2_5279.xml", ARActe.class);
        retourPrefectureService.receiveARActe(parsedData.getLeft(), parsedData.getRight());

        verify(acteRepository).findByMiatId("006-210600235-20180522-684-AI");
        verify(retourPrefectureService).publishActeHistory(eq("uuid"), eq(StatusType.ACK_RECEIVED),
                eq(parsedData.getRight()), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    public void testReceiveARAnnulation() throws IOException, JAXBException {

        given(acteRepository.findByMiatId(anyString()))
                .willReturn(Optional.of(acte()));

        Pair<ARAnnulation, Attachment> parsedData =
                parseClassPathResource("data/044-214400152-20181106-TEST_DGCL_4-DE-6-2_10.xml", ARAnnulation.class);
        retourPrefectureService.receiveARAnnulation(parsedData.getLeft(), parsedData.getRight());

        verify(acteRepository).findByMiatId("044-214400152-20181106-TEST_DGCL_4-DE");
        verify(retourPrefectureService).publishActeHistory(eq("uuid"), eq(StatusType.CANCELLED),
                eq(parsedData.getRight()), eq(Optional.empty()), eq(Optional.empty()));
    }

    @Test
    public void testReceiveAnomalieActe() throws IOException, JAXBException {

        given(localAuthorityRepository.findBySiren(any()))
                .willReturn(Optional.of(new LocalAuthority("uuid", "name", "siren", "name")));
        given(acteRepository.findFirstByNumberAndDecisionAndNatureAndLocalAuthority_UuidAndDraftNull(any(), any(), any(), any()))
                .willReturn(Optional.of(acte()));

        Pair<AnomalieActe, Attachment> parsedData =
                parseClassPathResource("data/044-214400152----1-3_1.xml", AnomalieActe.class);
        EnveloppeMISILLCL enveloppeMISILLCL =
                parseSingleClassPathResource("data/EACT--SPREF0441-214400152-20181012-9.xml",
                        EnveloppeMISILLCL.class);
        retourPrefectureService.receiveAnomalieActe(enveloppeMISILLCL, parsedData.getLeft(), parsedData.getRight());

        verify(localAuthorityRepository).findBySiren("214400152");
        verify(acteRepository).findFirstByNumberAndDecisionAndNatureAndLocalAuthority_UuidAndDraftNull(
                eq("123456"), eq(LocalDate.of(2018, 10, 1)), eq(ActeNature.DELIBERATIONS),
                eq("uuid"));
        verify(retourPrefectureService).publishActeHistory(eq("uuid"), eq(StatusType.NACK_RECEIVED),
                eq(parsedData.getRight()),
                eq(Optional.of("ODT-VERIF-01 : Votre acte a été transmis avec une date de classification qui n'est pas à jour")),
                eq(Optional.empty()));
    }

    @Test
    public void testReceiveAnomalieEnveloppe() throws IOException, JAXBException {

        given(acteHistoryRepository.findFirstByFileNameContaining(anyString()))
                .willReturn(Optional.of(new ActeHistory("uuid", StatusType.SENT)));
        given(acteRepository.findByUuidAndDraftNull(anyString()))
                .willReturn(Optional.of(acte()));

        Pair<AnomalieEnveloppe, Attachment> parsedData =
                parseClassPathResource("data/ANO_EACT--214400152--20180925-1.xml", AnomalieEnveloppe.class);
        retourPrefectureService.receiveAnomalieEnveloppe(parsedData.getLeft(), parsedData.getRight());

        verify(acteHistoryRepository).findFirstByFileNameContaining("214400152--20180925-1.tar.gz");
        verify(acteRepository).findByUuidAndDraftNull("uuid");
        verify(retourPrefectureService).publishActeHistory(eq("uuid"), eq(StatusType.NACK_RECEIVED),
                eq(parsedData.getRight()),
                eq(Optional.of("ODT-VERIF-17 : La transaction que vous avez envoyé n'est pas reconnu dans l'application ACTES.")),
                eq(Optional.empty()));
    }

    private <T> Pair<T, Attachment> parseClassPathResource(String path, Class<T> xmlClass) throws IOException, JAXBException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        InputStream is = classPathResource.getInputStream();
        byte[] resourceData = new byte[0];
        is.read(resourceData);

        StreamSource classSource = new StreamSource(classPathResource.getInputStream());
        T xmlData = XmlUtils.unmarshall(classSource, xmlClass);

        Attachment attachment = new Attachment(resourceData, classPathResource.getFilename(),
                classPathResource.getFile().length());

        return Pair.of(xmlData, attachment);
    }

    private <T> T parseSingleClassPathResource(String path, Class<T> xmlClass) throws IOException, JAXBException {
        ClassPathResource classPathResource = new ClassPathResource(path);
        StreamSource classSource = new StreamSource(classPathResource.getInputStream());
        return XmlUtils.unmarshall(classSource, xmlClass);
    }
}
