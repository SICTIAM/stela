package fr.sictiam.stela.convocationservice.service.eventlistener;

import fr.sictiam.stela.convocationservice.dao.AttachmentRepository;
import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.event.FileUploadEvent;
import fr.sictiam.stela.convocationservice.service.StorageService;
import fr.sictiam.stela.convocationservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class FileUploadEventListener {

    private final static Logger LOGGER = LoggerFactory.getLogger(FileUploadEventListener.class);

    private final StorageService storageService;

    private final AttachmentRepository attachmentRepository;

    @Autowired
    public FileUploadEventListener(
            StorageService storageService,
            AttachmentRepository attachmentRepository) {
        this.storageService = storageService;
        this.attachmentRepository = attachmentRepository;
    }


    @Async
    @EventListener
    public void storeAttachment(FileUploadEvent event) {
        Attachment attachment = event.getAttachment();
        try {
            storageService.storeAttachment(attachment);
            // drop content from DB when file is uploaded
            attachment.setContent(null);
            attachmentRepository.save(attachment);
        } catch (StorageException e) {
            LOGGER.error("Failed to store attachement {} ({}), keep it in DB", attachment.getFilename(), e.getMessage());
        }
    }
}
