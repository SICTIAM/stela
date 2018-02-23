package fr.sictiam.signature.pes.producer;

import fr.sictiam.signature.pes.verifier.DigestUtils;
import fr.sictiam.signature.pes.verifier.UnExpectedException;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

public class SigningPolicies {
    private static final String SIG_POLICY_ZIP_FILE = "/fr/sictiam/signature/pes/signingPolicies.zip";
    private static final SigningPolicies instance = new SigningPolicies();
    private Map<String, SigningPolicy1> signingPolicyByIdentifier;

    private SigningPolicies() {
        try {
            Map<String, SigningPolicy1> signingPolicyByCode = new HashMap();
            JarInputStream jarIS = new JarInputStream(
                    getClass().getResource("/fr/sictiam/signature/pes/signingPolicies.zip").openStream());
            try {
                JarEntry entry = null;
                while ((entry = jarIS.getNextJarEntry()) != null) {
                    if (!entry.isDirectory()) {
                        String signingPolicyFileNameWithoutExtension = entry.getName().substring(0,
                                entry.getName().lastIndexOf("."));
                        SigningPolicy1 signingPolicy = signingPolicyByCode.get(signingPolicyFileNameWithoutExtension);
                        if (signingPolicy == null) {
                            signingPolicy = new SigningPolicy1();
                            signingPolicyByCode.put(signingPolicyFileNameWithoutExtension, signingPolicy);
                        }
                        if (entry.getName().endsWith(".dat")) {
                            signingPolicy.loadMetaData(jarIS);
                        } else {
                            signingPolicy.setContentZipEntryName(entry.getName());
                        }
                    }
                }
                signingPolicyByIdentifier = new HashMap();
                for (SigningPolicy1 signingPolicy : signingPolicyByCode.values()) {
                    signingPolicyByIdentifier.put(signingPolicy.getIdentifier(), signingPolicy);
                }
            } finally {
                String signingPolicyFileNameWithoutExtension;
                jarIS.close();
            }
        } catch (Exception e) {
            throw new UnExpectedException(e);
        }
    }

    public static SigningPolicies getInstance() {
        return instance;
    }

    public Collection<SigningPolicy1> getSigningPolicies() {
        return signingPolicyByIdentifier.values();
    }

    public InputStream getContentInputStream(String contentZipEntryName) throws IOException {
        JarInputStream jarIS = new JarInputStream(
                getClass().getResource("/fr/sictiam/signature/pes/signingPolicies.zip").openStream());
        JarEntry entry = null;
        while ((entry = jarIS.getNextJarEntry()) != null) {
            if ((!entry.isDirectory()) && (entry.getName().equals(contentZipEntryName))) {
                return jarIS;
            }
        }
        return null;
    }

    public SigningPolicy1 getByIdentifier(String identifier) {
        return signingPolicyByIdentifier.get(identifier);
    }

    public static class SigningPolicy1 {
        private Properties metaData;
        private String contentZipEntryName;

        private SigningPolicy1() {
            metaData = new Properties();
        }

        private void setContentZipEntryName(String contentZipEntryName) {
            this.contentZipEntryName = contentZipEntryName;
        }

        private void loadMetaData(InputStream inputStream) throws IOException {
            metaData.clear();
            metaData.load(inputStream);
        }

        public InputStream getContentInputStream() throws IOException {
            return SigningPolicies.getInstance().getContentInputStream(contentZipEntryName);
        }

        public String getDescription() {
            return metaData.getProperty("Description");
        }

        public String getDigestValue() {
            return metaData.getProperty("DigestValue");
        }

        public String getDigestMethod() {
            return metaData.getProperty("DigestMethod");
        }

        public String getIdentifier() {
            return metaData.getProperty("Identifier");
        }

        public String getSigPolicyQualifier() {
            return metaData.getProperty("SigPolicyQualifier");
        }

        public String getFileName() {
            return contentZipEntryName.substring(contentZipEntryName.lastIndexOf("/") + 1);
        }

        public String computeDigestValue(String disgestMethod) throws IOException, NoSuchAlgorithmException {
            InputStream inputStream = getContentInputStream();
            try {
                return DigestUtils.xmlBase64Digest(inputStream, disgestMethod);
            } finally {
                inputStream.close();
            }
        }

        /* Error */
        public void display() {
            // Byte code:
            // 0: aload_0
            // 1: invokevirtual 22
            // fr/sictiam/signature/pes/producer/SigningPolicies$SigningPolicy:getContentInputStream
            // ()Ljava/io/InputStream;
            // 4: astore_1
            // 5: aload_1
            // 6: aload_0
            // 7: invokevirtual 25
            // fr/sictiam/signature/pes/producer/SigningPolicies$SigningPolicy:getFileName
            // ()Ljava/lang/String;
            // 10: invokestatic 26
            // fr/sictiam/signature/utils/FileUtils:createTemporaryFileFromStream
            // (Ljava/io/InputStream;Ljava/lang/String;)Ljava/io/File;
            // 13: astore_2
            // 14: invokestatic 27 java/awt/Desktop:getDesktop ()Ljava/awt/Desktop;
            // 17: aload_2
            // 18: invokevirtual 28 java/awt/Desktop:open (Ljava/io/File;)V
            // 21: aload_1
            // 22: invokevirtual 24 java/io/InputStream:close ()V
            // 25: goto +10 -> 35
            // 28: astore_3
            // 29: aload_1
            // 30: invokevirtual 24 java/io/InputStream:close ()V
            // 33: aload_3
            // 34: athrow
            // 35: goto +13 -> 48
            // 38: astore_1
            // 39: new 30 fr/sictiam/signature/pes/verifier/UnExpectedException
            // 42: dup
            // 43: aload_1
            // 44: invokespecial 31
            // fr/sictiam/signature/pes/verifier/UnExpectedException:<init>
            // (Ljava/lang/Throwable;)V
            // 47: athrow
            // 48: return
            // Line number table:
            // Java source line #145 -> byte code offset #0
            // Java source line #147 -> byte code offset #5
            // Java source line #148 -> byte code offset #14
            // Java source line #150 -> byte code offset #21
            // Java source line #151 -> byte code offset #25
            // Java source line #150 -> byte code offset #28
            // Java source line #154 -> byte code offset #35
            // Java source line #152 -> byte code offset #38
            // Java source line #153 -> byte code offset #39
            // Java source line #155 -> byte code offset #48
            // Local variable table:
            // start length slot name signature
            // 0 49 0 this SigningPolicy
            // 4 26 1 inputStream InputStream
            // 38 6 1 ioe IOException
            // 13 5 2 signingPolicyFile java.io.File
            // 28 6 3 localObject Object
            // Exception table:
            // from to target type
            // 5 21 28 finally
            // 0 35 38 java/io/IOException
        }

        @Override
        public String toString() {
            return getDescription();
        }
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.producer.SigningPolicies
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */