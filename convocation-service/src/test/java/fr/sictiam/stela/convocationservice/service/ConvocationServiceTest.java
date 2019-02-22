package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.dao.AttachmentRepository;
import fr.sictiam.stela.convocationservice.dao.ConvocationRepository;
import fr.sictiam.stela.convocationservice.dao.QuestionResponseRepository;
import fr.sictiam.stela.convocationservice.dao.RecipientResponseRepository;
import fr.sictiam.stela.convocationservice.model.AssemblyType;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Question;
import fr.sictiam.stela.convocationservice.model.QuestionResponse;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.exception.ConvocationCancelledException;
import fr.sictiam.stela.convocationservice.model.exception.NotFoundException;
import fr.sictiam.stela.convocationservice.service.util.PdfGeneratorUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import java.time.LocalDateTime;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@RunWith(SpringRunner.class)
public class ConvocationServiceTest {

    @SpyBean
    ConvocationService convocationService;

    @MockBean
    ConvocationRepository convocationRepository;

    @MockBean
    LocalAuthorityService localAuthorityService;

    @MockBean
    StorageService storageService;

    @MockBean
    ExternalRestService externalRestService;

    @MockBean
    RecipientResponseRepository recipientResponseRepository;

    @MockBean
    QuestionResponseRepository questionResponseRepository;

    @MockBean
    AttachmentRepository attachmentRepository;

    @MockBean
    ApplicationEventPublisher applicationEventPublisher;

    @MockBean
    private EntityManager entityManager;

    @MockBean
    EntityManagerFactory entityManagerFactory;

    @MockBean
    PdfGeneratorUtil pdfGeneratorUtil;

    @Before
    public void setUp() {
        Convocation convocation = createDummyConvocation();
        doReturn(convocation).when(convocationService).getConvocation(any(), any());
    }

    @Test
    public void questionOrder() {
        Convocation convocation = convocationService.getConvocation("anything", "anything");
        long i = 1;

        for (Iterator<Question> it = convocation.getQuestions().iterator(); it.hasNext(); i++) {
            Question q = it.next();
            assertEquals(q.getRank().longValue(), i);
        }
    }

    @Test
    public void questionResponseFalse() {

        Convocation convocation = convocationService.getConvocation("anything", "anything");

        given(questionResponseRepository.save(any()))
                .willReturn(new QuestionResponse());

        convocationService.answerQuestion(convocation, createRecipient(), "question-uuid-one", false);
        assertFalse(convocation.getQuestions().stream().findFirst().get().getResponses().stream().findFirst().get().getResponse());

        verify(questionResponseRepository).save(argThat(questionResponse ->
                !questionResponse.getResponse() &&
                        questionResponse.getQuestion().getUuid().equals("question-uuid-one") &&
                        questionResponse.getRecipient().getEmail().equals("firstname.lastname@mail.com")));
    }

    @Test
    public void questionResponseTrue() {

        Convocation convocation = convocationService.getConvocation("anything", "anything");

        given(questionResponseRepository.save(any()))
                .willReturn(new QuestionResponse());

        convocationService.answerQuestion(convocation, createRecipient(), "question-uuid-one", true);
        assertTrue(convocation.getQuestions().stream().findFirst().get().getResponses().stream().findFirst().get().getResponse());

        verify(questionResponseRepository).save(argThat(questionResponse ->
                questionResponse.getResponse() &&
                        questionResponse.getQuestion().getUuid().equals("question-uuid-one") &&
                        questionResponse.getRecipient().getEmail().equals("firstname.lastname@mail.com")));
    }

    @Test(expected = NotFoundException.class)
    public void questionResponseNotFound() {

        Convocation convocation = convocationService.getConvocation("anything", "anything");
        convocationService.answerQuestion(convocation, createRecipient(), "unknown-question-uuid", true);
    }

    @Test
    public void cancelConvocation() {

        Convocation convocation = convocationService.getConvocation("anything", "anything");
        convocationService.cancelConvocation(convocation);

        assertTrue(convocation.isCancelled());

        verify(convocationRepository).save(argThat(Convocation::isCancelled));
    }

    @Test(expected = ConvocationCancelledException.class)
    public void cancelConvocationTwice() {

        Convocation convocation = convocationService.getConvocation("anything", "anything");
        convocationService.cancelConvocation(convocation);
        convocationService.cancelConvocation(convocation);
    }

    @Test
    public void updateConvocation() {
        Convocation convocation = convocationService.getConvocation("anything", "anything");

        SortedSet<Question> questions = new TreeSet<>();
        questions.add(new Question("question three ?", 3));

        Convocation updateParams = new Convocation();
        updateParams.setComment("comment updated");
        updateParams.setQuestions(questions);

        convocation = convocationService.update("anything", "anything", updateParams);

        verify(convocationRepository).saveAndFlush(argThat(c -> c.getComment().equals("comment updated") &&
                c.getQuestions().size() == 3));
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

        Question question1 = new Question();
        ReflectionTestUtils.setField(question1, "uuid", "question-uuid-one");
        question1.setQuestion("test question one ?");
        question1.setRank(1);
        questions.add(question1);

        Question question2 = new Question();
        ReflectionTestUtils.setField(question2, "uuid", "question-uuid-two");
        question2.setQuestion("test question two ?");
        question2.setRank(2);
        questions.add(question2);

        convocation.setQuestions(questions);

        return convocation;
    }

    private static Recipient createRecipient() {
        Recipient recipient = new Recipient();
        ReflectionTestUtils.setField(recipient, "uuid", "recipient-uuid");
        recipient.setFirstname("firstname");
        recipient.setLastname("lastname");
        recipient.setEmail("firstname.lastname@mail.com");
        recipient.setToken("recipient-token");
        return recipient;
    }
}
