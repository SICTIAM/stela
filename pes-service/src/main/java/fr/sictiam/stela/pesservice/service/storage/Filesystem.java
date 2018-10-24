package fr.sictiam.stela.pesservice.service.storage;

import com.google.common.collect.ImmutableMap;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

@Service
public class Filesystem implements StorageDriver {

    private static final Logger LOGGER = LoggerFactory.getLogger(Filesystem.class);

    private final Environment environment;

    private String rootPath;

    public Filesystem(Environment environment) throws StorageException {

        this.environment = environment;
        rootPath = environment.getProperty("application.storage.filesystem.path");
        if (rootPath == null)
            throw new StorageException("Missing configuration property application.storage.filesystem.path");

        if (rootPath.endsWith("/"))
            rootPath = rootPath.substring(rootPath.length() - 1);

        Path path = Paths.get(rootPath);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.error("Failed to create directory {}: {}", rootPath, e.getMessage());
                throw new StorageException("Failed to create directory " + rootPath, e);
            }
        }
    }

    @Override
    public byte[] getObject(String key) throws StorageException {

        try {
            Path path = Paths.get(rootPath, key);
            return Files.readAllBytes(path);
        } catch (InvalidPathException e) {
            LOGGER.error("File not found {}/{}", rootPath, key);
            throw new StorageException("Key " + key + " not found", e);
        } catch (IOException e) {
            LOGGER.error("Failed to read {}/{}: {}", rootPath, key, e.getMessage());
            throw new StorageException("Failed to read " + key + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void storeObject(String key, byte[] content, String filename) throws StorageException {

        storeObject(key, content, ImmutableMap.of("filename", filename));
    }

    @Override
    public void storeObject(String key, byte[] content, Map<String, String> metaData) throws StorageException {

        try {
            Path path = Paths.get(rootPath, key);
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.write(path, content);
        } catch (InvalidPathException e) {
            LOGGER.error("Invalid path {}/{}: {}", rootPath, key, e.getMessage());
            throw new StorageException("Key " + key + " not found", e);
        } catch (IOException e) {
            LOGGER.error("Failed to write data in  {}/{}: {}", rootPath, key, e.getMessage());
            throw new StorageException("Failed to read " + key + ": " + e.getMessage(), e);
        }
    }
}
