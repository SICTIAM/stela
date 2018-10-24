package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import fr.sictiam.stela.pesservice.service.storage.StorageEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private StorageEngine storageEngine;

    public byte[] getObject(String key) throws StorageException {
        return storageEngine.getObject(key);
    }

    public void storeObject(String key, byte[] content, String filename) throws StorageException {
        storageEngine.storeObject(key, content, filename);
    }

    public byte[] getAttachmentContent(Attachment attachment) throws StorageException {

        // Don't read bytes everytime
        if (attachment.getContent() != null)
            return attachment.getContent();

        // load data from storage driver
        try {
            byte[] content = getObject(attachment.getStorageKey());
            // store content in memory
            attachment.setContent(content);
            return content;
        } catch (StorageException e) {
            LOGGER.error("Failed to retrieve attachment content for attachment {}: {}", attachment.getUuid(), e.getMessage());
            return null;
        }
    }

    public Attachment createAttachment(MultipartFile file) throws IOException {

        return createAttachment(file.getOriginalFilename(), file.getBytes());
    }

    public Attachment createAttachment(String filename, byte[] content) {
        return createAttachment(filename, content, LocalDateTime.now());
    }

    public Attachment createAttachment(String filename, byte[] content, LocalDateTime date) {
        Attachment attachment = new Attachment(filename, content, content.length, date);
        attachmentRepository.saveAndFlush(attachment);
        storeObject(attachment.getStorageKey(), content, filename);

        return attachment;
    }

    public Attachment updateAttachment(Attachment attachment, byte[] content) {
        attachment.updateContent(content);
        storeObject(attachment.getStorageKey(), content, attachment.getFilename());
        return attachment;
    }

    public boolean deleteAttachmentContent(Attachment attachment) {

        try {
            return storageEngine.deleteObject(attachment.getStorageKey());
        } catch (StorageException e) {
            return false;
        }
    }
}
