package fr.sictiam.stela.pesservice.service.storage;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.google.common.collect.ImmutableMap;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

@Service
@Profile({ "integration", "prod", "test", "atd24" })
public class AwsS3 implements StorageEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(AwsS3.class);

    @Value("${application.storage.awss3.bucket}")
    private String bucket;

    @Value("${application.storage.awss3.region}")
    private String region;

    @Value("${application.storage.awss3.accesskey}")
    private String accessKey;

    @Value("${application.storage.awss3.secretkey}")
    private String secretKey;

    private AmazonS3 s3;

    public AwsS3() {
    }

    @PostConstruct
    private void init() {
        s3 = AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)))
                .withRegion(region).build();

    }

    @Override
    public byte[] getObject(String key) throws StorageException {
        try {
            S3Object o = s3.getObject(bucket, key);
            S3ObjectInputStream s3is = o.getObjectContent();
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] read_buf = new byte[1024];
            int read_len;
            while ((read_len = s3is.read(read_buf)) > 0) {
                baos.write(read_buf, 0, read_len);
            }
            s3is.close();
            baos.close();
            return baos.toByteArray();
        } catch (AmazonServiceException e) {
            LOGGER.error("Key {} not found in bucket {}: {}", key, bucket, e.getMessage());
            throw new StorageException("Key " + key + " not found", e);
        } catch (IOException e) {
            LOGGER.error("Failed to read key {}: {}", key, e.getMessage());
            throw new StorageException("Failed to read key " + key + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void storeObject(String key, byte[] content, String filename) throws StorageException {
        LOGGER.info("Storing {} in {}/{}", filename, bucket, key);
        storeObject(key, content, ImmutableMap.of("filename", filename));
    }

    @Override
    public void storeObject(String key, byte[] content, Map<String, String> metaData) throws StorageException {

        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(content);
            ObjectMetadata objectMetaData = new ObjectMetadata();
            objectMetaData.setContentLength(content.length);
            objectMetaData.setUserMetadata(metaData);

            PutObjectResult res = s3.putObject(bucket, key, bais, objectMetaData);
            bais.close();

        } catch (IOException | AmazonServiceException e) {
            LOGGER.error("Failed to store {} in bucket {}: ", key, bucket, e.getMessage());
            throw new StorageException("Failed to store " + key + " in bucket " + bucket + ": " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deleteObject(String key) throws StorageException {

        try {
            s3.deleteObject(bucket, key);
            return true;
        } catch (AmazonServiceException e) {
            LOGGER.error("Failed to delete object {} in bucket {}: ", key, bucket, e.getMessage());
        }
        return false;
    }
}
