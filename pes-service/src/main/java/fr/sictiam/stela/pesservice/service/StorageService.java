package fr.sictiam.stela.pesservice.service;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import fr.sictiam.stela.pesservice.service.storage.StorageDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

@Service
public class StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageService.class);

    private final Environment environment;
    private final AttachmentRepository attachmentRepository;

    private StorageDriver storageDriver;

    public StorageService(Environment environment, AttachmentRepository attachmentRepository) throws StorageException {

        this.environment = environment;
        this.attachmentRepository = attachmentRepository;
        String driver = null;

        try {
            driver = environment.getProperty("application.storage.driver");
            Class clazz = Class.forName(driver);
            Constructor constr = clazz.getConstructor(Environment.class);
            storageDriver = (StorageDriver) constr.newInstance(environment);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Storage driver class not found {}: {}", driver, e.getMessage());
            throw new StorageException("Failed to instantiate storage driver " + driver + ": " + e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            LOGGER.error("Construtor not found in driver {}: {}", driver, e.getMessage());
            throw new StorageException("Failed to instantiate storage driver " + driver + ": " + e.getMessage(), e);
        } catch (InvocationTargetException e) {
            LOGGER.error("Constructor in threw an exception {}: {}", driver, e.getMessage());
            throw new StorageException("Failed to instantiate storage driver " + driver + ": " + e.getMessage(), e);
        } catch (InstantiationException e) {
            LOGGER.error("Tried to instantiate an abstract class : driver {}: {}", driver, e.getMessage());
            throw new StorageException("Failed to instantiate storage driver " + driver + ": " + e.getMessage(), e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Constructor in storage driver {} is inaccessible: {}", driver, e.getMessage());
            throw new StorageException("Failed to instantiate storage driver " + driver + ": " + e.getMessage(), e);
        }
    }

    public byte[] getObject(String key) throws StorageException {
        return storageDriver.getObject(key);
    }

    public void storeObject(String key, byte[] content, String filename) throws StorageException {
        storageDriver.storeObject(key, content, filename);
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

    public void deleteAttachment(Attachment attachment) {
        // TODO

    }
}
