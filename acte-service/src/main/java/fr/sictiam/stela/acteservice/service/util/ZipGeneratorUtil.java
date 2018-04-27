package fr.sictiam.stela.acteservice.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class ZipGeneratorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZipGeneratorUtil.class);

    public byte[] createZip(Map<String, byte[]> files) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream out = new ZipOutputStream(baos);
        for (Map.Entry<String, byte[]> entry : files.entrySet()) {
            ZipEntry e = new ZipEntry(entry.getKey());
            out.putNextEntry(e);
            out.write(entry.getValue(), 0, entry.getValue().length);
            out.closeEntry();
        }
        out.close();
        return baos.toByteArray();
    }
}
