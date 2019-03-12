package fr.sictiam.stela.apigateway.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import java.io.ByteArrayOutputStream;

public final class LocalFileUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(LocalFileUtils.class);

    public static String getLocalFile(String filePath){
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {
            ClassPathResource resource = new ClassPathResource(filePath);
            FileCopyUtils.copy(resource.getInputStream(), bos);
        } catch (Exception e) {
            LOGGER.error("Unable to load json translation file: {}", e);
        }
        return bos.toString();
    }
}
