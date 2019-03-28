package fr.sictiam.stela.pesservice.controller;

import fr.sictiam.stela.pesservice.model.GenericDocument;
import fr.sictiam.stela.pesservice.model.LocalAuthority;
import fr.sictiam.stela.pesservice.model.sesile.Classeur;
import fr.sictiam.stela.pesservice.model.sesile.ClasseurStatus;
import fr.sictiam.stela.pesservice.model.sesile.Document;
import fr.sictiam.stela.pesservice.model.ui.GenericAccount;
import fr.sictiam.stela.pesservice.service.ExternalRestService;
import fr.sictiam.stela.pesservice.service.LocalAuthorityService;
import fr.sictiam.stela.pesservice.service.PaullService;
import fr.sictiam.stela.pesservice.service.SesileService;
import fr.sictiam.stela.pesservice.service.util.CertUtilService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static fr.sictiam.stela.pesservice.TestDataGenerator.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.ResultMatcher.matchAll;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(PaullGenericController.class)
@ActiveProfiles("test")
public class PaullGenericControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PaullGenericController paullGenericController;

    @MockBean
    SesileService sesileService;

    @MockBean
    LocalAuthorityService localAuthorityService;

    @MockBean
    PaullService paullService;

    @MockBean
    CertUtilService certUtilService;

    @MockBean
    ExternalRestService externalRestService;

    private String urlTemplate = "/rest/externalws/SIREN/fr/classic/webservgeneriques/services/api/rest.php/";

    private HttpHeaders httpHeaders;

    private LocalAuthority localAuthority;


    @Before
    public void setUp() {
        this.httpHeaders = new HttpHeaders();
        this.httpHeaders.set("userid", "test1@test.fr");
        this.httpHeaders.set("password", "test1password");

        given(paullService.emailAuth("test1@test.fr", "test1password")).willReturn(new GenericAccount());

        this.localAuthority =
                new LocalAuthority("uuid-local-authority-one", "local authority one", "250601879", true);
        localAuthority.setSesileNewVersion(true);

        given(localAuthorityService.getBySiren("250601879")).willReturn(Optional.of(localAuthority));

        given(sesileService
                .checkClasseurStatus(this.localAuthority, 3000))
                .willReturn(new ResponseEntity<>(this.createDummyClasseur(), HttpStatus.OK));

        GenericDocument genericDocument = new GenericDocument();

        genericDocument.setDepositEmail("deposit@test.fr");

        given(sesileService.getGenericDocument(any())).willReturn(Optional.of(genericDocument));
    }

    @Test
    public void infosDocument() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/infosdoc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isOk(),
                        jsonPath("$.status").value(200),
                        jsonPath("$.status_message").value("OK"),
                        jsonPath("$.data").hasJsonPath(),
                        jsonPath("$.data.EtatClasseur").value(ClasseurStatus.IN_PROGRESS.toString())))
                .andDo(print());
    }

    @Test
    public void infosDocumentNotFoundLocalAuthority() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys123456789") + "/infosdoc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("LocalAuthority was not found"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void infosDocumentNotFoundClasseur() throws Exception {
        given(sesileService.checkClasseurStatus(this.localAuthority, 1000)).willReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/infosdoc/1000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("Classeur was not found"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void infosDocumentFoundClasseurAndNotFoundDocumentGeneric() throws Exception {
        given(sesileService.getGenericDocument(3000)).willReturn(Optional.empty());

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/infosdoc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("Classeur was not found"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void infosDocumentErrorOccurredWhenSearchingClasseur() throws Exception {
        Classeur classeur = null;

        given(sesileService.checkClasseurStatus(this.localAuthority, 3000)).willReturn(new ResponseEntity<>(classeur, HttpStatus.OK));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/infosdoc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("Error occurred while searching Classeur"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void infosDocumentNoDocumentInClasseur() throws Exception {
        Classeur classeur = new Classeur();

        classeur.setNom("classeur test 1");
        classeur.setStatus(ClasseurStatus.IN_PROGRESS);
        classeur.setValidation("21/02/2022");
        classeur.setCircuit("circuit test 1");
        classeur.setCreation("20/02/2022");
        classeur.setDocuments(new ArrayList<>());

        given(sesileService.checkClasseurStatus(this.localAuthority, 3000)).willReturn(new ResponseEntity<>(classeur, HttpStatus.OK));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/infosdoc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("No document in Classeur"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void getDocument() throws Exception {
        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/doc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isOk(),
                        jsonPath("$.status").value(200),
                        jsonPath("$.status_message").value("OK"),
                        jsonPath("$.data").hasJsonPath(),
                        jsonPath("$.data.NomDocument").isNotEmpty()))
                .andDo(print());
    }

    @Test
    public void getDocumentLocalAuthorityNotFound() throws Exception {
        given(sesileService.checkClasseurStatus(this.localAuthority, 1000)).willReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys123456789") + "/doc/1000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("LocalAuthority was not found"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void getDocumentClasseurNotFound() throws Exception {
        given(sesileService.checkClasseurStatus(this.localAuthority, 1000)).willReturn(new ResponseEntity<>(HttpStatus.NOT_FOUND));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/doc/1000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("Classeur was not found"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void getDocumentErrorOccurredWhenSearchingClasseur() throws Exception {
        Classeur classeur = null;

        given(sesileService.checkClasseurStatus(this.localAuthority, 3000)).willReturn(new ResponseEntity<>(classeur, HttpStatus.OK));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/doc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("Error occurred while searching Classeur"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    @Test
    public void getDocumentNoDocumentInClasseur() throws Exception {
        Classeur classeur = new Classeur();

        classeur.setNom("classeur test 1");
        classeur.setStatus(ClasseurStatus.IN_PROGRESS);
        classeur.setValidation("21/02/2022");
        classeur.setCircuit("circuit test 1");
        classeur.setCreation("20/02/2022");
        classeur.setDocuments(new ArrayList<>());

        given(sesileService.checkClasseurStatus(this.localAuthority, 3000)).willReturn(new ResponseEntity<>(classeur, HttpStatus.OK));

        ResultActions resultActions =
                mockMvc.perform(
                        get(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/doc/3000")
                                .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isBadRequest(),
                        jsonPath("$.status").value(400),
                        jsonPath("$.status_message").value("No document in Classeur"),
                        jsonPath("$.data").hasJsonPath()))
                .andDo(print());
    }

    /*
     * Very minimal test to be completed later on
     */
    @Test
    public void shouldCreateADocWithADefaultEmail() throws Exception {

        given(paullService.emailAuth(any(), any()))
                .willReturn(new GenericAccount());
        given(localAuthorityService.getBySiren(any()))
                .willReturn(localAuthority());
        given(externalRestService.getProfile(any()))
                .willReturn(profileNode());
        given(sesileService.postClasseur(any(), any(), any()))
                .willReturn(classeur(LocalDateTime.now()));
        given(sesileService.addFileToclasseur(any(), any(), any(), anyInt()))
                .willReturn(new Document());

        ResultActions resultActions =
                mockMvc.perform(multipart(urlTemplate.replaceFirst("SIREN", "sys250601879") + "/depot")
                        .file("file", "file".getBytes())
                        .param("title", "Mon bon de commande")
                        .param("service", "12")
                        .param("type", "2")
                        .headers(httpHeaders));

        resultActions
                .andExpect(matchAll(
                        status().isOk()
                ));

        verify(localAuthorityService).getBySiren("250601879");
        verify(externalRestService).getProfile("generic-profile-uuid");
        verify(sesileService).postClasseur(any(),
                argThat(classeurRequest -> classeurRequest.getEmail().equals("agent@sictiam.fr")),
                any());
    }

    private Classeur createDummyClasseur() {
        Document document = new Document();

        document.setName("document test 1");

        List<Document> documents = new ArrayList<>();
        documents.add(document);

        Classeur classeur = new Classeur();

        classeur.setNom("classeur test 1");
        classeur.setStatus(ClasseurStatus.IN_PROGRESS);
        classeur.setValidation("21/02/2022");
        classeur.setCircuit("circuit test 1");
        classeur.setCreation("20/02/2022");
        classeur.setDocuments(documents);

        return classeur;
    }

}