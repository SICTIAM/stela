package fr.sictiam.stela.pesservice.service.storage;

import fr.sictiam.stela.pesservice.service.exceptions.StorageException;

import java.util.Map;

public interface StorageEngine {

    byte[] getObject(String key) throws StorageException;

    void storeObject(String key, byte[] content, String filename) throws StorageException;

    void storeObject(String key, byte[] content, Map<String, String> metaData) throws StorageException;

    boolean deleteObject(String key) throws StorageException;
}
