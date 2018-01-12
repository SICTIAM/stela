package fr.sictiam.stela.acteservice.scheduler;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.PostConstruct;
import javax.validation.constraints.NotNull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;

import fr.sictiam.stela.acteservice.model.PendingMessage;
import fr.sictiam.stela.acteservice.model.event.ActeHistoryEvent;
import fr.sictiam.stela.acteservice.service.ActeService;
import fr.sictiam.stela.acteservice.service.PendingMessageService;

@Component
public class SenderTask implements ApplicationListener<ActeHistoryEvent> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SenderTask.class);

    private Queue<PendingMessage> pendingQueue = new ConcurrentLinkedQueue<>();

    @Value("${application.archive.maxSizePerHour}")
    private Integer maxSizePerHour;
    
    @Value("${application.miat.url}")
    private String acteUrl;   

    private AtomicInteger currentSizeUsed = new AtomicInteger();

    @Autowired
    private ActeService acteService;

    @Autowired
    private PendingMessageService pendingMessageService;
    
    @Autowired
    @Qualifier("miatRestTemplate")
    private RestTemplate miatRestTemplate;

    @PostConstruct
    public void initQueue() {
        pendingQueue.addAll(pendingMessageService.getAllPendingMessages());
    }

    @Override
    public void onApplicationEvent(@NotNull ActeHistoryEvent event) {
        switch (event.getActeHistory().getStatus()) {
            case ARCHIVE_SIZE_CHECKED:
                pendingQueue.add(pendingMessageService.save(new PendingMessage(event.getActeHistory())));
                break;
        }
    }
    
    //reset limitation every hour
    @Scheduled(cron = "0 0 0/1 * * ?")
    public void resetLimitation() {
        currentSizeUsed.set(0);
    }

    @Scheduled(fixedRate = 100)
    public void senderTask() {

        if (!pendingQueue.isEmpty()) {
            PendingMessage pendingMessage = pendingQueue.peek();
            if ((pendingMessage.getFile().length + currentSizeUsed.get()) < maxSizePerHour) {

                HttpStatus sendStatus = null;
                try {
                    sendStatus = send(pendingMessage.getFile(), pendingMessage.getFileName());
                } catch (Exception e) {
                    sendStatus = HttpStatus.INTERNAL_SERVER_ERROR;
                }
                if (HttpStatus.OK.equals(sendStatus)) {
                    acteService.sent(pendingMessage.getActeUuid());
                    pendingMessageService.remove(pendingQueue.poll());
                    currentSizeUsed.addAndGet(pendingMessage.getFile().length);
                    LOGGER.info("Amount of data sent for this hour : " + currentSizeUsed);
                } else if (HttpStatus.NOT_FOUND.equals(sendStatus)) {
                    // pref offline
                    // just keep retrying
                } else if (HttpStatus.BAD_REQUEST.equals(sendStatus)
                        || HttpStatus.INTERNAL_SERVER_ERROR.equals(sendStatus)) {
                    // something wrong in what we send
                    // TODO when prefecture sending is "plugged", look if we can extract some useful info about the error
                    acteService.notSent(pendingMessage.getActeUuid());
                    pendingMessageService.remove(pendingQueue.poll());
                }
            } else {
                LOGGER.info("Hourly limit exceeded, waiting next hour");
            }
        }
    }
    
    public HttpStatus send(byte[] file, String fileName) throws Exception {

        System.setProperty("javax.net.debug", "all");
        
        File outputFile = new File(fileName);
        FileOutputStream fileOuputStream = new FileOutputStream(outputFile); 
        fileOuputStream.write(file);
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        
        map.add("file", outputFile);
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-User-Agent", "stela");
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<LinkedMultiValueMap<String, Object>>(
                map, headers);

        ResponseEntity<String> result = miatRestTemplate.exchange(acteUrl, HttpMethod.POST,
                requestEntity, String.class);
        outputFile.delete();
        return result.getStatusCode();
    }
}
