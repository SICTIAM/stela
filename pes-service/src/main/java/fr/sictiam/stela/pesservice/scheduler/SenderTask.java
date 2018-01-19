package fr.sictiam.stela.pesservice.scheduler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.integration.ftp.session.DefaultFtpSessionFactory;
import org.springframework.integration.ftp.session.FtpSession;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import fr.sictiam.stela.pesservice.dao.PendingMessageRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.model.PendingMessage;
import fr.sictiam.stela.pesservice.model.PesAller;
import fr.sictiam.stela.pesservice.model.StatusType;
import fr.sictiam.stela.pesservice.model.event.PesHistoryEvent;
import fr.sictiam.stela.pesservice.service.PesAllerService;

@Component
public class SenderTask implements ApplicationListener<PesHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderTask.class);

    private Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    @Value("${application.archive.maxSizePerHour}")
    private Integer maxSizePerHour;

    private AtomicInteger currentSizeUsed = new AtomicInteger();

    @Autowired
    private PesAllerService pesService;

    @Autowired
    private PendingMessageRepository pendingMessageRepository;

    @Autowired
    private DefaultFtpSessionFactory defaultFtpSessionFactory;

    @PostConstruct
    public void initQueue() {
        pendingQueue.addAll(pendingMessageRepository.findAll());
    }

    @Override
    public void onApplicationEvent(@NotNull PesHistoryEvent event) {
        switch (event.getPesHistory().getStatus()) {
        case CREATED:
            pendingQueue.add(pendingMessageRepository.save(new PendingMessage(event.getPesHistory())));
            break;
        }
    }

    // reset limitation every hour
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void resetLimitation() {
        currentSizeUsed.set(0);
    }

    @Scheduled(fixedRate = 100)
    public void senderTask() {

        if (!pendingQueue.isEmpty()) {
            PendingMessage pendingMessage = pendingQueue.peek();
            PesAller pes = pesService.getByUuid(pendingMessage.getPesUuid());
            Attachment attachment = pes.getAttachment();

            if ((attachment.getFile().length + currentSizeUsed.get()) < maxSizePerHour) {

                StatusType sendStatus = null;
                try {
                    send(pes);
                    sendStatus = StatusType.SENT;
                    pendingMessageRepository.delete(pendingQueue.poll());
                    currentSizeUsed.addAndGet(pendingMessage.getFile().length);
                } catch (Exception e) {
                    sendStatus = StatusType.NOT_SENT;
                }
                pesService.updateStatus(pendingMessage.getPesUuid(), sendStatus);

            } else {
                LOGGER.info("Hourly limit exceeded, waiting next hour");
            }
        }
    }

    public void send(PesAller pes)
            throws IOException, XPathExpressionException, SAXException, ParserConfigurationException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pes.getAttachment().getFile());
        FtpSession ftpSession = defaultFtpSessionFactory.getSession();
        FTPClient ftpClient = ftpSession.getClientInstance();
        ftpClient.sendSiteCommand("quote site P_DEST " + pes.getLocalAuthority().getServerCode());
        ftpClient.sendSiteCommand("quote site P_APPLI GHELPES2");

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(byteArrayInputStream);

        XPathFactory xpf = XPathFactory.newInstance();

        XPath path = xpf.newXPath();

        String colCode = path.evaluate("/PES_Aller/EnTetePES/CodCol/@V", document);
        String postId = path.evaluate("/PES_Aller/EnTetePES/IdPost/@V", document);
        String budCode = path.evaluate("/PES_Aller/EnTetePES/CodBud/@V", document);
        ftpClient.sendSiteCommand("quote site P_MSG PESALR2#" + colCode + "#" + postId + "#" + budCode + "");
        ftpSession.write(byteArrayInputStream, pes.getAttachment().getFilename());
        ftpSession.close();

    }

}
