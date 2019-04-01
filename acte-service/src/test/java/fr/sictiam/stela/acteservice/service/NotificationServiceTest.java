package fr.sictiam.stela.acteservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.sictiam.stela.acteservice.SmtpServerRule;
import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import org.apache.commons.mail.util.MimeMessageParser;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.thymeleaf.TemplateEngine;

import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.IOException;

import static fr.sictiam.stela.acteservice.utils.ActeUtils.*;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.emptyArray;
import static org.junit.Assert.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doReturn;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = { "spring.main.allow-bean-definition-overriding = true",
                        "spring.mail.port = 2525",
                        "spring.mail.host = localhost" },
        classes = { NotificationService.class })
@Import(MailSenderAutoConfiguration.class)
@ActiveProfiles("test")
public class NotificationServiceTest {

    @SpyBean
    private NotificationService notificationService;

    @Autowired
    private JavaMailSender javaMailSender;

    @MockBean
    private ActeService acteService;

    @MockBean
    private ExternalRestService externalRestService;

    @MockBean
    private TemplateEngine template;

    @MockBean
    private LocalesService localesService;

    @Rule
    public SmtpServerRule smtpServerRule = new SmtpServerRule(2525);

    @Test
    public void shouldSendEmailToCreatorWhenActeIsSent() throws Exception {

        Acte acte = acte();
        acte.setLocalAuthority(localAuthority());
        given(acteService.getByUuid(any()))
                .willReturn(acte);
        given(externalRestService.getProfiles(any()))
                .willReturn(profilesNode());
        given(externalRestService.getProfile(any()))
                .willReturn(profileNode());
        doReturn("Corps du mail")
                .when(notificationService).processTemplate(any(), any(), any());
        given(localesService.getMessage(any(), any(), any()))
                .willReturn("Sujet du mail");

        ActeHistory history = new ActeHistory(acte.getUuid(), StatusType.SENT);
        ActeHistoryEvent mockEvent = new ActeHistoryEvent(this, history);
        notificationService.proccessEvent(mockEvent);

        MimeMessage[] receivedMessages = smtpServerRule.getMessages();
        assertThat(receivedMessages, not(emptyArray()));
        assertThat(receivedMessages.length, is(1));
        MimeMessage current = receivedMessages[0];
        assertThat(current, notNullValue());
        MimeMessageParser parser = new MimeMessageParser(current);
        parser.parse();
        assertThat(parser.getSubject(), is("Sujet du mail"));
        assertThat(current.getContent(), instanceOf(MimeMultipart.class));
        assertThat(parser.getHtmlContent(), is("Corps du mail"));
    }

    private JsonNode profilesNode() throws IOException {
        String profile = "[]";
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readTree(profile);
    }
}
