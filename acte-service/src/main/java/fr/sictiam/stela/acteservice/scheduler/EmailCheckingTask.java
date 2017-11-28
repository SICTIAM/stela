package fr.sictiam.stela.acteservice.scheduler;

import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.sun.mail.util.MailSSLSocketFactory;

import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.xml.ARActe;
import fr.sictiam.stela.acteservice.model.xml.ARAnnulation;
import fr.sictiam.stela.acteservice.model.xml.EnveloppeMISILLCL;
import fr.sictiam.stela.acteservice.model.xml.RetourClassification;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.LocalAuthorityService;
import fr.sictiam.stela.acteservice.service.exceptions.NoEnveloppeException;

@Component
public class EmailCheckingTask {

    private static Store store;
    private static Folder inbox;
    private static Folder archiveBox;
    private static Folder errorBox;

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailCheckingTask.class);

    @Autowired
    private ActeService acteService;

    @Autowired
    private LocalAuthorityService localAuthorityService;

    @Value("${application.checkingemail.host}")
    private String host;

    @Value("${application.checkingemail.username}")
    private String username;

    @Value("${application.checkingemail.password}")
    private String password;

    @PostConstruct
    public void init() {

        try {

            Properties properties = new Properties();
            MailSSLSocketFactory sf = new MailSSLSocketFactory();
            sf.setTrustAllHosts(true);
            properties.put("mail.imap.ssl.trust", "*");
            properties.put("mail.imap.ssl.socketFactory", sf);
            properties.put("mail.imap.host", host);
            properties.put("mail.imap.port", "993");
            properties.put("mail.imap.ssl.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);
            store = emailSession.getStore("imap");
            store.connect(host, username, password);
            inbox = store.getFolder("INBOX");

            archiveBox = store.getFolder("DONE");
            archiveBox.open(Folder.READ_WRITE);

            errorBox = store.getFolder("ERROR");
            errorBox.open(Folder.READ_WRITE);

        } catch (NoSuchProviderException e) {
            LOGGER.error(e.getMessage());
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        } catch (GeneralSecurityException e) {
            LOGGER.error(e.getMessage());
        }

    }

    @PreDestroy
    public void clean() {
        try {
            archiveBox.close(false);
            errorBox.close(false);
            store.close();
        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        }

    }

    @Scheduled(fixedRate = 30000)
    public void check() {
        try {

            inbox.open(Folder.READ_WRITE);
            Message[] messages = inbox.getMessages();
            LOGGER.debug("messages.length---" + messages.length);

            List<Message> messagesOK = new ArrayList<>();
            List<Message> messagesKO = new ArrayList<>();
            for (int i = 0; i < messages.length; i++) {
                Message message = messages[i];

                if (!message.isSet(Flag.DELETED)) {
                    try {
                        LOGGER.debug("---------------------------------");
                        LOGGER.debug("Email Number " + (i + 1));
                        LOGGER.debug("Subject: " + message.getSubject());
                        LOGGER.debug("From: " + message.getFrom()[0]);
                        LOGGER.debug("Text: " + message.getContent().toString());

                        Multipart multipart = (Multipart) message.getContent();

                        EnveloppeMISILLCL enveloppe = null;

                        for (int j = 0; j < multipart.getCount(); j++) {
                            BodyPart bodyPart = multipart.getBodyPart(j);
                            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                                    && StringUtils.isEmpty(bodyPart.getFileName())) {
                                continue; // dealing with attachments only
                            }
                            InputStream is = bodyPart.getInputStream();

                            JAXBContext jc = JAXBContext.newInstance(String.class);
                            Unmarshaller unmarshaller = jc.createUnmarshaller();
                            StreamSource xmlSource = new StreamSource(is);
                            JAXBElement<String> je = unmarshaller.unmarshal(xmlSource, String.class);

                            String rootName = je.getName().getLocalPart();

                            StreamSource xmlSourc2 = new StreamSource(bodyPart.getInputStream());
                            if ("ARActe".equals(rootName)) {
                                ARActe arActe = unmarshall(xmlSourc2, ARActe.class);
                                acteService.receiveARActe(arActe.getIDActe());

                            } else if ("ARAnnulation".equals(rootName)) {
                                ARAnnulation arAnnulation = unmarshall(xmlSourc2, ARAnnulation.class);
                                acteService.receiveARActeCancelation(arAnnulation.getIDActe());

                            } else if ("RetourClassification".equals(rootName)) {
                                RetourClassification retClassification = unmarshall(xmlSourc2,
                                        RetourClassification.class);

                                if (enveloppe == null) {
                                    throw new NoEnveloppeException();
                                }
                                LocalAuthority currentLocalAuthority = localAuthorityService
                                        .getBySiren(enveloppe.getDestinataire().getSIREN()).get();

                                localAuthorityService.loadCodesMatieres(currentLocalAuthority.getUuid(),
                                        retClassification);
                            } else if ("EnveloppeMISILLCL".equals(rootName)) {
                                enveloppe = unmarshall(xmlSourc2, EnveloppeMISILLCL.class);
                            }
                        }
                        message.setFlag(Flag.DELETED, true);
                        messagesOK.add(message);
                    } catch (Exception e) {
                        message.setFlag(Flag.DELETED, true);
                        messagesKO.add(message);
                        LOGGER.error(e.getMessage());
                    }

                }
            }
            inbox.copyMessages(messagesOK.toArray(new Message[messagesOK.size()]), archiveBox);
            inbox.copyMessages(messagesKO.toArray(new Message[messagesKO.size()]), errorBox);

        } catch (MessagingException e) {
            LOGGER.error(e.getMessage());
        } finally {
            try {
                inbox.close(false);
            } catch (MessagingException e1) {
                LOGGER.error(e1.getMessage());
            }
        }
    }

    protected static <T> T unmarshall(StreamSource xml, Class<T> clazz) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        T obj = clazz.cast(unmarshaller.unmarshal(xml));
        return obj;
    }
}
