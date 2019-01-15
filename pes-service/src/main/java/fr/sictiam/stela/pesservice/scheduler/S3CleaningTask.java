package fr.sictiam.stela.pesservice.scheduler;

import fr.sictiam.stela.pesservice.dao.AttachmentRepository;
import fr.sictiam.stela.pesservice.model.Attachment;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import fr.sictiam.stela.pesservice.service.storage.AwsS3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.util.Optional;

@Component
@Profile("S3")
public class S3CleaningTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(S3CleaningTask.class);

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private AwsS3 s3Service;

    @Scheduled(cron = "${application.storage.awss3.cleaningCron}")
    public void purgeObjects() {

        LOGGER.info("Start S3 cleaning");
        boolean done = false;
        ListObjectsV2Response response;
        long count = 0L;
        long deleted = 0L;
        String token = null;
        while (!done) {
            try {
                response = s3Service.listObjects(token);
                count += response.keyCount();

                for (S3Object content : response.contents()) {
                    Optional<Attachment> a = attachmentRepository.findByStorageKey(content.key());
                    if (!a.isPresent()) {
                        if (s3Service.deleteObject(content.key())) {
                            deleted++;
                        }
                    }
                }

                if ((token = response.nextContinuationToken()) == null) done = true;

            } catch (StorageException e) {
                // thrown by listObjects()
                LOGGER.error("Failed to retrieve S3 objects: {}. Stop processing", e.getMessage());
            }
        }

        LOGGER.info("Found {} objects | deleted {}", count, deleted);
    }
}
