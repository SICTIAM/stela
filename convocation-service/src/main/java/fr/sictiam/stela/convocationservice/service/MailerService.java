package fr.sictiam.stela.convocationservice.service;

import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.service.exceptions.MailException;
import org.springframework.stereotype.Service;

@Service
public interface MailerService {

    void sendEmail(String address, String subject, String body) throws MailException;

    void sendEmail(String address, String subject, String body, Profile from) throws MailException;

    void sendEmail(String address, String subject, String body, Profile from, Attachment document) throws MailException;
}
