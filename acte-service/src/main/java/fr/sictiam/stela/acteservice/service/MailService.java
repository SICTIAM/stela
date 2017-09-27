package fr.sictiam.stela.acteservice.service;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.mail.Address;
import javax.mail.BodyPart;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeMessage.RecipientType;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

@Service
public class MailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MailService.class);

    private String hostname = "mail.sictiam.fr";
    private String port = "110";
    private String login = "stela3dev@sictiam.fr";
    private String password = "-j8XY32q(e";

    private List<String> forwardAdresses = Arrays.asList("yannprosper@free.fr");
    
    @Scheduled(fixedDelay = 5000, initialDelay = 5000)
    public void checkMail() {
        //LOGGER.info("Started mail checking at {}", LocalDateTime.now());

        Properties props = new Properties();

        props.put("mail.pop3.host", hostname);
        props.put("mail.pop3.port", port);
        props.put("mail.smtp.host", "mail.sictiam.fr");

        Session session = Session.getDefaultInstance(props);

        try {
            Store store = session.getStore("pop3");
            store.connect(hostname, login, password);

            Folder inbox = store.getFolder("INBOX");
            inbox.open(Folder.READ_WRITE);

            Message[] messages = inbox.getMessages();

            for (Message message : messages) {
                if (message.isMimeType("multipart/*")) {
                    Multipart multipart = (Multipart) message.getContent();

                    Part enveloppe = null;
                    Map<String, Part> filesToParse = new HashMap<String, Part>();

                    for (int i = 0; i < multipart.getCount(); i++) {
                        Part part = multipart.getBodyPart(i);
                        if (part.isMimeType("text/xml") || part.isMimeType("application/xml")) {
                            String filename = part.getFileName();
                            if (filename.startsWith("EACT")) {
                                enveloppe = part;
                            } else {
                                filesToParse.put(filename, part);
                            }
                        }
                    }

                    if (enveloppe != null) {
                        Document document = openXmlDocument(enveloppe.getInputStream());

                        Node main = document.getElementsByTagName("actes:FormulairesEnvoyes").item(0);
                        NodeList formulaires = main.getChildNodes();

                        for (int j = 0; j < formulaires.getLength(); j++) {
                            Node formulaire = formulaires.item(j);
                            String filename = formulaire.getTextContent().trim();
                            if (!filename.isEmpty()) {
                                String regex = "^.{3}-\\d{9}-\\d{8}-.{1,15}-.{2}-(\\d)-(\\d)_\\d{1,4}\\.xml$";
                                Pattern pattern = Pattern.compile(regex);
                                Matcher m = pattern.matcher(filename);

                                if (m.find()) {
                                    // TODO: better check of the Transmission/Flux
                                    if (m.group(1).equalsIgnoreCase("1") && m.group(2).equalsIgnoreCase("2")) {
                                        manageAR(filename, filesToParse.get(filename));
                                    }
                                } else {
                                    LOGGER.info("No transaction code is matching for {}", filename);
                                }
                            }
                        }
                    }
                    // TODO: works with my smtp server but not yet with sictiam's.
                    //forward(message);
                    // TODO: will be uncommented when needed.
                    //message.setFlag(Flag.DELETED, true);
                }
            }

            inbox.close();
            store.close();

            //LOGGER.info("Mails checked at {}", LocalDateTime.now());
        } catch (MessagingException e) {
            LOGGER.error("Messaging exception" + e.getMessage());
        } catch (IOException io) {
            LOGGER.error("IO Exception" + io.getMessage());
        } catch (ParserConfigurationException pa) {
            LOGGER.error("Parse Exception" + pa.getMessage());
        } catch (SAXException sa) {
            LOGGER.error("SAX Exception" + sa.getMessage());
        }
    }

    private void manageAR(String filename, Part part)
            throws MessagingException, ParserConfigurationException, SAXException, IOException {
        
        try (InputStream input = part.getDataHandler().getInputStream()) {
            Document document = openXmlDocument(input);

            Node recu = document.getElementsByTagName("actes:ActeRecu").item(0);
            NamedNodeMap attr = recu.getAttributes();
            Node n = attr.getNamedItem("actes:NumeroInterne");
        }
    }

    private Document openXmlDocument(InputStream inputStream) 
            throws MessagingException, ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document result = builder.parse(inputStream);
        result.getDocumentElement().normalize();

        return result;
    }

    private void forward(Message m)
            throws MessagingException, ParserConfigurationException, SAXException, IOException {
        Message toSend = new MimeMessage(m.getSession());

        toSend.setSubject("FWD: " + m.getSubject());
        toSend.addFrom(m.getFrom());
        for(String address : forwardAdresses){
            toSend.addRecipient(RecipientType.TO, new InternetAddress(address));
        }

        BodyPart bodyPart = new MimeBodyPart();
        bodyPart.setText("Sent by Acte Module Stela.");

        Multipart multipart = new MimeMultipart();
        multipart.addBodyPart(bodyPart);

        bodyPart = new MimeBodyPart();
        bodyPart.setDataHandler(m.getDataHandler());
        multipart.addBodyPart(bodyPart);

        toSend.setContent(multipart);

        Transport.send(toSend);
    }
}