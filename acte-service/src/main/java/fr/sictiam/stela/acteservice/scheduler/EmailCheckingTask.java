package fr.sictiam.stela.acteservice.scheduler;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.mail.BodyPart;
import javax.mail.Flags.Flag;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
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

import fr.sictiam.stela.acteservice.model.Attachment;
import fr.sictiam.stela.acteservice.model.LocalAuthority;
import fr.sictiam.stela.acteservice.model.StatusType;
import fr.sictiam.stela.acteservice.model.xml.ARActe;
import fr.sictiam.stela.acteservice.model.xml.ARAnnulation;
import fr.sictiam.stela.acteservice.model.xml.ARReponseCL;
import fr.sictiam.stela.acteservice.model.xml.AnomalieActe;
import fr.sictiam.stela.acteservice.model.xml.CourrierSimple;
import fr.sictiam.stela.acteservice.model.xml.DefereTA;
import fr.sictiam.stela.acteservice.model.xml.DemandePieceComplementaire;
import fr.sictiam.stela.acteservice.model.xml.EnveloppeMISILLCL;
import fr.sictiam.stela.acteservice.model.xml.LettreObservations;
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

    @Value("${application.email.listening.host}")
    private String host;

    @Value("${application.email.listening.port}")
    private Integer port;

    @Value("${application.email.username}")
    private String username;

    @Value("${application.email.password}")
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
            properties.put("mail.imap.port", port);
            properties.put("mail.imap.ssl.enable", "true");
            Session emailSession = Session.getDefaultInstance(properties);
            store = emailSession.getStore("imap");
            store.connect(host, username, password);
            inbox = store.getFolder("INBOX");

            archiveBox = store.getFolder("DONE");
            archiveBox.open(Folder.READ_WRITE);

            errorBox = store.getFolder("ERROR");
            errorBox.open(Folder.READ_WRITE);

        } catch (MessagingException | GeneralSecurityException e) {
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

                        if (multipart.getCount() > 1 && multipart.getContentType().contains("ALTERNATIVE")) {
                            for (int k = 0; k < multipart.getCount(); k++) {
                                BodyPart bodyPart = multipart.getBodyPart(k);
                                if (bodyPart.getContent() instanceof Multipart) {
                                    multipart = (Multipart) multipart.getBodyPart(1).getContent();
                                    break;
                                }
                            }
                        }

                        final Multipart originalMultipart = multipart;
                        for (int j = 0; j < originalMultipart.getCount(); j++) {
                            BodyPart bodyPart = originalMultipart.getBodyPart(j);
                            if (!Part.ATTACHMENT.equalsIgnoreCase(bodyPart.getDisposition())
                                    && StringUtils.isEmpty(bodyPart.getFileName())
                                    && StringUtils.endsWithIgnoreCase(bodyPart.getFileName(), ".xml")) {
                                continue; // dealing with attachments only
                            }
                            if (StringUtils.endsWithIgnoreCase(bodyPart.getFileName(), ".xml")) {
                                InputStream is = bodyPart.getInputStream();

                                JAXBContext jc = JAXBContext.newInstance(String.class);
                                Unmarshaller unmarshaller = jc.createUnmarshaller();
                                StreamSource xmlSource = new StreamSource(is);
                                JAXBElement<String> je = unmarshaller.unmarshal(xmlSource, String.class);

                                String rootName = je.getName().getLocalPart();

                                StreamSource classSource = new StreamSource(bodyPart.getInputStream());
                                if ("ARActe".equals(rootName)) {
                                    ARActe arActe = unmarshall(classSource, ARActe.class);
                                    acteService.receiveARActe(arActe.getIDActe());

                                } else if ("ARAnnulation".equals(rootName)) {
                                    ARAnnulation arAnnulation = unmarshall(classSource, ARAnnulation.class);
                                    acteService.receiveARActeCancelation(arAnnulation.getIDActe());

                                } else if ("AnomalieActe".equals(rootName)) {
                                    if (enveloppe == null) {
                                        throw new NoEnveloppeException();
                                    }
                                    AnomalieActe anomalie = unmarshall(classSource, AnomalieActe.class);

                                    acteService.receiveAnomalie(enveloppe.getDestinataire().getSIREN(),
                                            anomalie.getActeRecu().getNumeroInterne(), anomalie.getDetail());

                                } else if ("CourrierSimple".equals(rootName)) {
                                    CourrierSimple courrierSimple = unmarshall(classSource, CourrierSimple.class);
                                    Attachment attachment = getFileAttachmentByName(
                                            courrierSimple.getDocument().getNomFichier(), originalMultipart);
                                    acteService.receiveAdditionalPiece(StatusType.COURRIER_SIMPLE_RECEIVED,
                                            courrierSimple.getIDActe(), attachment, null);

                                } else if ("DemandePieceComplementaire".equals(rootName)) {
                                    DemandePieceComplementaire demandePieceComplementaire = unmarshall(classSource,
                                            DemandePieceComplementaire.class);
                                    demandePieceComplementaire.getIDActe();
                                    Attachment attachment = getFileAttachmentByName(
                                            demandePieceComplementaire.getDocument().getNomFichier(),
                                            originalMultipart);
                                    acteService.receiveAdditionalPiece(StatusType.DEMANDE_PIECE_COMPLEMENTAIRE_RECEIVED,
                                            demandePieceComplementaire.getIDActe(), attachment,
                                            demandePieceComplementaire.getDescriptionPieces());
                                } else if ("LettreObservations".equals(rootName)) {
                                    LettreObservations letterObs = unmarshall(classSource, LettreObservations.class);
                                    Attachment attachment = getFileAttachmentByName(
                                            letterObs.getDocument().getNomFichier(), originalMultipart);
                                    acteService.receiveAdditionalPiece(StatusType.LETTRE_OBSERVATION_RECEIVED,
                                            letterObs.getIDActe(), attachment, letterObs.getMotif());

                                } else if ("DefereTA".equals(rootName)) {
                                    DefereTA defereTA = unmarshall(classSource, DefereTA.class);
                                    List<Attachment> attachments = defereTA.getPiecesJointes().getPieceJointe().stream()
                                            .map(file -> getFileAttachmentByName(file.getNomFichier(),
                                                    originalMultipart))
                                            .collect(Collectors.toList());
                                    
                                    acteService.receiveDefere(StatusType.DEFERE_RECEIVED, defereTA.getIDActe(), attachments, defereTA.getNatureIllegalite());

                                } else if ("ARPieceComplementaire".equals(rootName)) {
                                    ARReponseCL arPieceComplementaire = unmarshall(classSource, ARReponseCL.class);

                                } else if ("ARReponseRejetLettreObservations".equals(rootName)) {
                                    ARReponseCL arLettreObs = unmarshall(classSource, ARReponseCL.class);

                                } else if ("RetourClassification".equals(rootName)) {
                                    RetourClassification retClassification = unmarshall(classSource,
                                            RetourClassification.class);

                                    if (enveloppe == null) {
                                        throw new NoEnveloppeException();
                                    }
                                    LocalAuthority currentLocalAuthority = localAuthorityService
                                            .getBySiren(enveloppe.getDestinataire().getSIREN()).get();

                                    localAuthorityService.loadClassification(currentLocalAuthority.getUuid(),
                                            retClassification);
                                } else if ("EnveloppeMISILLCL".equals(rootName)) {
                                    enveloppe = unmarshall(classSource, EnveloppeMISILLCL.class);
                                }
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

    Attachment getFileAttachmentByName(String name, Multipart multipart) {
        try {
            for (int j = 0; j < multipart.getCount(); j++) {
                BodyPart bodyPart = multipart.getBodyPart(j);
                if (name.equals(bodyPart.getFileName())) {
                    InputStream inputStream = bodyPart.getInputStream();
                    byte[] targetArray = new byte[inputStream.available()];
                    inputStream.read(targetArray);
                    return new Attachment(targetArray, name, bodyPart.getSize());
                }
            }
        } catch (MessagingException | IOException e) {
            LOGGER.error(e.getMessage());
        }

        return null;
    }

    protected static <T> T unmarshall(StreamSource xml, Class<T> clazz) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(clazz);
        Unmarshaller unmarshaller = jc.createUnmarshaller();
        T obj = clazz.cast(unmarshaller.unmarshal(xml));
        return obj;
    }
}
