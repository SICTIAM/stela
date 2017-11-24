package fr.sictiam.stela.acteservice.service.util;

import javax.net.ssl.*;
import java.security.Security;

public class SecurityProtocols {

    static {
        Security.setProperty("jdk.tls.disabledAlgorithms", "");
    }

    public static void main(String[] args) throws Exception {

        SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
        SSLSocket soc = (SSLSocket) factory.createSocket();

        // Returns the names of the protocol versions which are
        // currently enabled for use on this connection.
        String[] protocols = soc.getEnabledProtocols();

        System.out.println("Enabled protocols:");
        for (String s : protocols) {
            System.out.println(s);
        }

    }
}