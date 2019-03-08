package fr.sictiam.stela.convocationservice.service.mail;

import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.service.MailerService;
import fr.sictiam.stela.convocationservice.service.exceptions.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import java.io.UnsupportedEncodingException;

@Component
public class JavaMail implements MailerService {

    private final static Logger LOGGER = LoggerFactory.getLogger(JavaMail.class);

    private final JavaMailSender mailer;

    @Autowired
    public JavaMail(JavaMailSender javaMailSender) {
        mailer = javaMailSender;
    }

    @Override
    public void sendEmail(String address, String subject, String body) throws MailException {
        sendEmail(address, subject, body, null, null);
    }

    @Override
    public void sendEmail(String address, String subject, String body, Profile from) throws MailException {
        sendEmail(address, subject, body, from, null);
    }

    @Override
    public void sendEmail(String address, String subject, String body, Profile from, Attachment document)
            throws MailException {
        try {
            MimeMessage message = mailer.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setSubject(subject);
            helper.setText(body, true);
            helper.setTo(address);
            if (from != null) {
                try {
                    helper.setFrom(from.getEmail(), from.getFullName());
                } catch (UnsupportedEncodingException e) {
                    LOGGER.warn("Encoding error in author name : {} : {}", from.getFullName(), e.getMessage());
                }
            }

            if (document != null && document.getContent() != null) {
                helper.addAttachment(document.getFilename(), new ByteArrayResource(document.getContent()));
            }

            mailer.send(message);
        } catch (MessagingException | MailSendException e) {
            LOGGER.error("Error while sending mail to {} : {}", address, e.getMessage());
            throw new MailException("Error while sending mail to " + address + " : " + e.getMessage());
        }
    }
}
