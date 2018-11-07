package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public interface StorageService {

    static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    byte[] getObject(String key) throws StorageException;

    void storeObject(String key, byte[] content, String filename) throws StorageException;

    boolean deleteObject(String key) throws StorageException;

    public default byte[] getAttachmentContent(Attachment attachment) throws StorageException {

        // Don't read bytes everytime
        if (attachment.getContent() != null)
            return attachment.getContent();

        // load data from storage driver
        try {
            LOGGER.info("Loading {} content", attachment.getFilename());
            byte[] content = getObject(attachment.getStorageKey());
            // store content in memory
            attachment.setContent(content);
            LOGGER.info("Attachment content fully loaded");
            return content;
        } catch (StorageException e) {
            LOGGER.error("Failed to retrieve attachment content for attachment {}: {}", attachment.getUuid(), e.getMessage());
            return null;
        }
    }

    public default Attachment createAttachment(MultipartFile file) throws StorageException, IOException {

        return createAttachment(file.getOriginalFilename(), file.getBytes());
    }

    public default Attachment createAttachment(String filename, byte[] content) throws StorageException {
        return createAttachment(filename, content, LocalDateTime.now());
    }

    public default Attachment createAttachment(String filename, byte[] content, LocalDateTime date) throws StorageException {
        Attachment attachment = new Attachment(filename, content, content.length, date);
        storeObject(attachment.getStorageKey(), content, filename);

        return attachment;
    }

    public default void storeAttachment(Attachment attachment) throws StorageException {
        storeObject(attachment.getStorageKey(), attachment.getContent(), attachment.getFilename());
    }

    public default Attachment updateAttachment(Attachment attachment, byte[] content) throws StorageException {
        attachment.updateContent(content);
        storeObject(attachment.getStorageKey(), content, attachment.getFilename());
        return attachment;
    }

    public default boolean deleteAttachmentContent(Attachment attachment) {

        try {
            return deleteObject(attachment.getStorageKey());
        } catch (StorageException e) {
            return false;
        }
    }
}
