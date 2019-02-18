package fr.sictiam.stela.convocationservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import fr.sictiam.stela.convocationservice.dao.ConvocationRepository;
import fr.sictiam.stela.convocationservice.dao.RecipientRepository;
import fr.sictiam.stela.convocationservice.model.AssemblyType;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Question;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.Right;
import fr.sictiam.stela.convocationservice.service.ConvocationService;
import fr.sictiam.stela.convocationservice.service.ExternalRestService;
import fr.sictiam.stela.convocationservice.service.RecipientService;
import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import javax.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ConvocationRestController.class)
public class ConvocationRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ConvocationRestController convocationRestController;

    @MockBean
    private ConvocationService convocationService;

    @MockBean
    private RecipientService recipientService;

    @MockBean
    private ExternalRestService externalRestService;

    @MockBean
    private RecipientRepository recipientRepository;

    @MockBean
    private ConvocationRepository convocationRepository;

    @MockBean
    private EntityManagerFactory entityManagerFactory;


    @Before
    public void setUp() {
        Convocation convocation = createDummyConvocation();
        given(convocationService.getConvocation("convocation-uuid-one", "mairie-test")).willReturn(convocation);
        given(convocationService.getConvocation("convocation-uuid-one")).willReturn(convocation);
    }


    @Test
    public void getConvocation() throws Exception {
        Set<Right> rights = new HashSet<>();
        rights.add(Right.CONVOCATION_DISPLAY);
        Assertions.assertThat(convocationRestController).isNotNull();

        Convocation convocation = createDummyConvocation();

        given(convocationService.getConvocation("uuid-convocation-test-one", "mairie-test")).willReturn(convocation);

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        mockMvc.perform(get("/api/convocation/uuid-convocation-test-one")
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.uuid").value(convocation.getUuid()))
                .andExpect(jsonPath("$.assemblyType").exists())
                .andExpect(jsonPath("$.location").value(convocation.getLocation()))
                .andExpect(jsonPath("$.meetingDate").value(formatter.format(convocation.getMeetingDate())))
                .andExpect(jsonPath("$.creationDate").value(formatter.format(convocation.getCreationDate())))
                .andExpect(jsonPath("$.questions").isArray());

        verify(convocationService, times(1)).getConvocation("uuid-convocation-test-one", "mairie-test");
        verifyNoMoreInteractions(convocationService);
    }

    @Test
    public void createSuccessfully() throws Exception {
        Set<Right> rights = new HashSet<>();
        rights.add(Right.CONVOCATION_DEPOSIT);

        given(convocationService.create(createDummyConvocation(), "mairie-test", "profile-one")).willReturn(createDummyConvocation());

        mockMvc.perform(post("/api/convocation")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(createDummyConvocationAsJsonString())
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isCreated());

    }

    @Test
    public void creationMissingFields() throws Exception {
        Set<Right> rights = new HashSet<>();
        rights.add(Right.CONVOCATION_DEPOSIT);

        ObjectMapper objectMapper = new ObjectMapper();

        Map<String, String> body = new HashMap<>();
        body.put("subject", "convocation test two");
        body.put("comment", "comment convocation test two");

        given(convocationService.create(createDummyConvocation(), "mairie-test", "profile-one")).willReturn(createDummyConvocation());
        mockMvc.perform(post("/api/convocation")
                .contentType(MediaType.APPLICATION_JSON_UTF8)
                .content(objectMapper.writeValueAsString(body))
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void answerQuestionBadParameter() throws Exception {

        Set<Right> rights = Collections.singleton(Right.CONVOCATION_DISPLAY);
        Convocation convocation = createDummyConvocation();

        given(convocationService.getConvocation("uuid-convocation-test-one", "mairie-test"))
                .willReturn(convocation);

        mockMvc.perform(put("/api/convocation/received/convocation-uuid-one/question/question-uuid-one/tru")
                .requestAttr("STELA-Current-Recipient", createRecipient())
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void notRecipientAnswersQuestion() throws Exception {

        Set<Right> rights = Collections.singleton(Right.CONVOCATION_DISPLAY);

        mockMvc.perform(put("/api/convocation/received/convocation-uuid-one/question/question-uuid-one/true")
                .requestAttr("STELA-Current-Recipient", createFakeRecipient())
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void answersQuestionWithCorrectFieldsAndRecipient() throws Exception {

        Set<Right> rights = Collections.singleton(Right.CONVOCATION_DISPLAY);

        mockMvc.perform(put("/api/convocation/received/convocation-uuid-one/question/question-uuid-one/true")
                .requestAttr("STELA-Current-Recipient", createRecipient())
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isOk());
    }

    @Test
    public void cancelConvocationOk() throws Exception {
        Set<Right> rights = Collections.singleton(Right.CONVOCATION_DEPOSIT);

        mockMvc.perform(put("/api/convocation/convocation-uuid-one/cancel")
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isOk());

        verify(convocationService).cancelConvocation(any());
    }

    @Test
    public void cancelConvocationInvalidRights() throws Exception {
        Set<Right> rights = Collections.singleton(Right.CONVOCATION_DISPLAY);

        mockMvc.perform(put("/api/convocation/convocation-uuid-one/cancel")
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void alreadyCancelledConvocation() throws Exception {

        given(convocationService.getConvocation("convocation-uuid-one", "mairie-test"))
                .willReturn(createCancelledConvocation());

        doCallRealMethod().when(convocationService).cancelConvocation(any());

        Set<Right> rights = Collections.singleton(Right.CONVOCATION_DEPOSIT);

        mockMvc.perform(put("/api/convocation/convocation-uuid-one/cancel")
                .requestAttr("STELA-Current-Profile-Rights", rights)
                .requestAttr("STELA-Current-Local-Authority-UUID", "mairie-test")
                .requestAttr("STELA-Current-Profile-UUID", "profile-one"))
                .andExpect(status().isConflict());

        verify(convocationService).cancelConvocation(any());
    }

    private static Convocation createDummyConvocation() {
        Convocation convocation = new Convocation();

        AssemblyType assemblyType = new AssemblyType();
        ReflectionTestUtils.setField(assemblyType, "uuid", "assembly-type-one");
        assemblyType.setName("Test assembly type one");

        SortedSet<Question> questions = new TreeSet<>();

        ReflectionTestUtils.setField(convocation, "uuid", "convocation-uuid-one");
        convocation.setSubject("convocation test one");
        convocation.setComment("comment convocation test one");
        convocation.setCreationDate(LocalDateTime.now());
        convocation.setLocation("mairie");
        convocation.setAssemblyType(assemblyType);
        convocation.setMeetingDate(LocalDateTime.now().plusDays(15));
        convocation.setProfileUuid("profile-one");


        RecipientResponse recipientResponse = new RecipientResponse();
        ReflectionTestUtils.setField(recipientResponse, "uuid", "recipient-response-uuid-one");
        recipientResponse.setRecipient(createRecipient());
        SortedSet<RecipientResponse> recipientResponses = new TreeSet<>();
        recipientResponses.add(recipientResponse);
        convocation.setRecipientResponses(recipientResponses);

        Question question = new Question();

        ReflectionTestUtils.setField(question, "uuid", "question-uuid-one");
        question.setQuestion("test question one ?");
        question.setRank(1);
        questions.add(question);

        convocation.setQuestions(questions);

        return convocation;
    }

    private static Convocation createCancelledConvocation() {
        Convocation convocation = createDummyConvocation();
        convocation.setCancelled(true);
        convocation.setCancellationDate(LocalDateTime.now());
        return convocation;
    }

    private static Recipient createRecipient() {
        Recipient recipient = new Recipient();
        ReflectionTestUtils.setField(recipient, "uuid", "recipient-uuid-one");
        recipient.setFirstname("firstname");
        recipient.setLastname("lastname");
        recipient.setEmail("firstname.lastname@mail.com");
        recipient.setToken("recipient-token");
        return recipient;
    }

    private static Recipient createFakeRecipient() {
        Recipient recipient = new Recipient();
        ReflectionTestUtils.setField(recipient, "uuid", "fake-recipient");
        recipient.setFirstname("firstname");
        recipient.setLastname("lastname");
        recipient.setEmail("fake.recipient@mail.com");
        recipient.setToken("fake-recipient-token");
        return recipient;
    }

    private static String createDummyConvocationAsJsonString() {
        final JsonNodeFactory factory = JsonNodeFactory.instance;

        final ObjectNode convocation = factory.objectNode();

        final ObjectNode assemblyType = factory.objectNode();

        assemblyType.put("uuid", "assembly-type-uuid-one");

        final ArrayNode recipients = factory.arrayNode();


        recipients.add(factory.objectNode().put("uuid", "recipient-uuid-one"));

        DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        convocation.set("assemblyType", assemblyType);
        convocation.set("recipients", recipients);
        convocation.put("subject", "Test assembly type one");
        convocation.put("comment", "Comment convocation test one");
        convocation.put("location", "Mairie");
        convocation.put("meetingDate", formatter.format(LocalDateTime.now().plusDays(15)));
        convocation.put("creationDate", formatter.format(LocalDateTime.now()));

        return convocation.toString();
    }
}