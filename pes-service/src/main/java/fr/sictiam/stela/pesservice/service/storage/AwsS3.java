package fr.sictiam.stela.pesservice.service.storage;

import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Request;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.annotation.PostConstruct;

@Component
@Profile("!(dev | dev-docker)")
public class AwsS3 implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsS3.class);

    @Value("${application.storage.awss3.bucket}")
    private String bucket;

    @Value("${application.storage.awss3.region}")
    private String region;

    @Value("${application.storage.awss3.accesskey}")
    private String accessKey;

    @Value("${application.storage.awss3.secretkey}")
    private String secretKey;

    private S3Client s3;

    public AwsS3() {
    }

    @PostConstruct
    private void init() {
        s3 = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    @Override
    public byte[] getObject(String key) throws StorageException {
        try {
            return s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build()).asByteArray();
        } catch (NoSuchKeyException e) {
            LOGGER.error("Key {} not found in bucket {}: {}", key, bucket, e.getMessage());
            throw new StorageException("Key " + key + " not found", e);
        } catch (SdkException e) {
            LOGGER.error("Failed to read key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to read key " + key + ": " + e.getMessage(), e);
        }
    }

    @Override public void storeObject(String key, byte[] content) throws StorageException {

        try {
            s3.putObject(PutObjectRequest.builder().bucket(bucket).key(key).build(), RequestBody.fromBytes(content));
        } catch (SdkException e) {
            LOGGER.error("Failed to store {} in bucket {}: ", key, bucket, e.getMessage());
            throw new StorageException("Failed to store " + key + " in bucket " + bucket + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteObject(String key) throws StorageException {

        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
            return true;
        } catch (SdkException e) {
            LOGGER.error("Failed to delete object {} in bucket {}: {}", key, bucket, e.getMessage());
        }
        return false;
    }

    public ListObjectsV2Response listObjects(String token) throws StorageException {

        try {
            return s3.listObjectsV2(ListObjectsV2Request.builder().bucket(bucket).prefix("pes").continuationToken(token).build());
        } catch (SdkException e) {
            LOGGER.error("Failed list objects in bucket {} with continuation token : ", bucket, token, e.getMessage());
            throw new StorageException("Failed list objects in bucket " + bucket + " with continuation token " + token, e);
        }
    }
}
