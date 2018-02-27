package fr.sictiam.signature.pes.verifier;

import org.apache.commons.io.IOUtils;
import org.apache.xml.security.utils.Base64;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;

public class DigestUtils {
    private static final Map<String, String> JAVA_DIGESTER_NAME_BY_XML_DIGESTER_NAME = new HashMap<>();
    private static final String SHA1PROTOCOL = "SHA-1";
    private static final String SHA256PROTOCOL = "SHA-256";
    private static final String SHA512PROTOCOL = "SHA-512";

    static {
        JAVA_DIGESTER_NAME_BY_XML_DIGESTER_NAME.put("http://www.w3.org/2000/09/xmldsig#sha1", "SHA-1");
        JAVA_DIGESTER_NAME_BY_XML_DIGESTER_NAME.put("http://www.w3.org/2001/04/xmlenc#sha256", "SHA-256");
        JAVA_DIGESTER_NAME_BY_XML_DIGESTER_NAME.put("http://www.w3.org/2001/04/xmlenc#sha512", "SHA-512");
        JAVA_DIGESTER_NAME_BY_XML_DIGESTER_NAME.put(null, "SHA-1");
    }

    public static byte[] xmlDigest(byte[] data, String xmlDigesterName) throws NoSuchAlgorithmException, IOException {
        return xmlDigest(new ByteArrayInputStream(data), xmlDigesterName);
    }

    public static byte[] xmlDigest(InputStream inputStream, String xmlDigesterName)
            throws NoSuchAlgorithmException, IOException {
        String javaDigester = JAVA_DIGESTER_NAME_BY_XML_DIGESTER_NAME.get(xmlDigesterName);
        if (javaDigester == null) {
            throw new NoSuchAlgorithmException(xmlDigesterName);
        }
        MessageDigest messageDigest = MessageDigest.getInstance(javaDigester);
        return messageDigest.digest(IOUtils.toByteArray(inputStream));
    }

    public static String xmlBase64Digest(byte[] data, String xmlDigesterName)
            throws NoSuchAlgorithmException, IOException {
        return xmlBase64Digest(new ByteArrayInputStream(data), xmlDigesterName);
    }

    public static String xmlBase64Digest(InputStream inputStream, String xmlDigesterName)
            throws NoSuchAlgorithmException, IOException {
        return Base64.encode(xmlDigest(inputStream, xmlDigesterName));
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.verifier.DigestUtils Java
 * Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */