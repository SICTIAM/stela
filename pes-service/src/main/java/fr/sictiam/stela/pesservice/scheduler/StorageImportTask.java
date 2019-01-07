package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import java.util.List;

@Transactional
@Component
public class StorageImportTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(StorageImportTask.class);

    @Autowired
    StorageService storageService;

    @Autowired
    AttachmentRepository attachmentRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Scheduled(fixedDelay = 60000)
    public void importAttachments() throws Exception {

        Query query = entityManager.createNativeQuery("select uuid from " +
                "attachment where file is not null limit 500");

        List<String> result = query.getResultList();
        LOGGER.info("Importing {} attachments", result.size());

        for (String uuid : result) {

            query = entityManager.createNativeQuery("SELECT uuid, filename, file FROM attachment WHERE uuid='" + uuid +
                    "'");
            List<Object[]> attachmentRow = query.getResultList();

            for (Object[] data : attachmentRow) {
                // Should be only one
                Attachment attachment = new Attachment();
                try {
                    attachment = storageService.createAttachment((String) data[1], (byte[]) data[2]);
                    Query update = entityManager.createNativeQuery("update attachment set storage_key=:key, file=NULL" +
                            " where uuid=:uuid");
                    update.setParameter("key", attachment.getStorageKey()).setParameter("uuid", (String) data[0]);
                    update.executeUpdate();
                } catch (StorageException e) {
                    LOGGER.error("Failed to store attachment {} ({}): {}", data[0], data[1], e.getMessage());
                } catch (Exception e) {
                    LOGGER.error("Database exception: {}, removing file {}", e.getMessage(), attachment.getStorageKey());
                    try {
                        storageService.deleteObject(attachment.getStorageKey());
                    } catch (StorageException se) {
                        LOGGER.error("Failed to remove attachment {}: {}", data[0], se.getMessage());
                    }
                }
            }
        }

        // Process data in pes_history table
        query = entityManager.createNativeQuery("SELECT uuid FROM pes_history WHERE file IS NOT NULL  limit 500");
        result = query.getResultList();
        LOGGER.info("Importing {} pes history attachments", result.size());

        for (String uuid : result) {

            query =
                    entityManager.createNativeQuery("SELECT uuid, file_name, file FROM pes_history WHERE uuid='" + uuid + "'");
            List<Object[]> historyRow = query.getResultList();

            for (Object[] data : historyRow) {
                Attachment attachment = new Attachment();
                try {
                    attachment = storageService.createAttachment((String) data[1], (byte[]) data[2]);
                    attachment = attachmentRepository.saveAndFlush(attachment);
                    Query update = entityManager.createNativeQuery("update pes_history set attachment_uuid=:auuid, " +
                            "file=NULL, file_name=NULL" +
                            " where uuid=:uuid");
                    update.setParameter("auuid", attachment.getUuid()).setParameter("uuid", (String) data[0]);
                    update.executeUpdate();
                } catch (StorageException e) {
                    LOGGER.error("Failed to store pes history attachment {} ({}): {}", data[0], data[1],
                            e.getMessage());
                } catch (Exception e) {
                    LOGGER.error("Database exception: {}, removing file {}", e.getMessage(), attachment.getStorageKey());
                    try {
                        storageService.deleteObject(attachment.getStorageKey());
                    } catch (StorageException se) {
                        LOGGER.error("Failed to remove pes history attachment {}: {}", data[0], se.getMessage());
                    }
                }
            }
        }

    }
}
