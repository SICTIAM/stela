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
import fr.sictiam.stela.pesservice.service.AdminService;
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
    
    @Autowired 
    private AdminService adminService;

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

        if (!pendingQueue.isEmpty() && adminService.isHeliosAvailable()) {
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
                    LOGGER.error(e.getMessage());
                    sendStatus = StatusType.NOT_SENT;
                }
                pesService.updateStatus(pendingMessage.getPesUuid(), sendStatus, attachment.getFile(), attachment.getFilename());

            } else {
                LOGGER.info("Hourly limit exceeded, waiting next hour");
            }
        }
    }

    public void send(PesAller pes) throws IOException  {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(pes.getAttachment().getFile());
        FtpSession ftpSession = defaultFtpSessionFactory.getSession();
        FTPClient ftpClient = ftpSession.getClientInstance();
        ftpClient.sendSiteCommand("quote site P_DEST " + pes.getLocalAuthority().getServerCode());
        ftpClient.sendSiteCommand("quote site P_APPLI GHELPES2");
        ftpClient.sendSiteCommand("quote site P_MSG " + pes.getFileType() + "#" + pes.getColCode() + "#"
                + pes.getPostId() + "#" + pes.getBudCode());
        ftpSession.write(byteArrayInputStream, pes.getAttachment().getFilename());
        ftpSession.close();
    }
}
