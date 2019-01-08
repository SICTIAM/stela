package fr.sictiam.stela.pesservice.service.storage;

import fr.sictiam.stela.pesservice.service.StorageService;
import fr.sictiam.stela.pesservice.service.exceptions.StorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@Profile("!S3")
public class Filesystem implements StorageService {

    private static final Logger LOGGER = LoggerFactory.getLogger(Filesystem.class);

    @Value("${application.storage.filesystem.path}")
    private String rootPath;

    public Filesystem() {
    }

    @PostConstruct
    public void init() {
        if (rootPath.endsWith("/"))
            rootPath = rootPath.substring(rootPath.length() - 1);

        Path path = Paths.get(rootPath);
        if (Files.notExists(path)) {
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                LOGGER.error("Failed to create directory {}: {}", rootPath, e.getMessage());
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

    @Override public void storeObject(String key, byte[] content) throws StorageException {

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

    @Override
    public boolean deleteObject(String key) throws StorageException {

        Path path = Paths.get(rootPath, key);
        try {
            return Files.deleteIfExists(path);
        } catch (IOException e) {
            LOGGER.error("Failed to remove file {}: {}", path, e.getMessage());
            throw new StorageException("Failed to remove file " + path + ": " + e.getMessage(), e);
        }
    }
}
