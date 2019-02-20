package fr.sictiam.stela.convocationservice.service.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class EmailChecker {

    private final static Logger LOGGER = LoggerFactory.getLogger(EmailChecker.class);

    private static int hear(BufferedReader in) throws IOException {
        String line = null;
        int res = 0;
        while ((line = in.readLine()) != null) {
            String pfx = line.substring(0, 3);
            try {
                res = Integer.parseInt(pfx);
            } catch (Exception ex) {
                res = -1;
            }
            if (line.charAt(3) != '-') break;
        }
        return res;
    }

    private static void say(BufferedWriter wr, String text)
            throws IOException {
        wr.write(text + "\r\n");
        wr.flush();
    }

    private static List<String> getMX(String hostName) throws NamingException {
        // Perform a DNS lookup for MX records in the domain
        Hashtable<String, String> env = new Hashtable<>();
        env.put("java.naming.factory.initial",
                "com.sun.jndi.dns.DnsContextFactory");
        DirContext ictx = new InitialDirContext(env);
        Attributes attrs = ictx.getAttributes(hostName, new String[]{ "MX" });
        Attribute attr = attrs.get("MX");
        // if we don't have an MX record, try the machine itself
        if ((attr == null) || (attr.size() == 0)) {
            attrs = ictx.getAttributes(hostName, new String[]{ "A" });
            attr = attrs.get("A");
            if (attr == null)
                throw new NamingException
                        ("No match for name '" + hostName + "'");
        }
        // Huzzah! we have machines to try. Return them as an array list
        // NOTE: We SHOULD take the preference into account to be absolutely
        //   correct. This is left as an exercise for anyone who cares.
        List<String> res = new ArrayList<>();
        NamingEnumeration en = attr.getAll();
        while (en.hasMore()) {
            String x = (String) en.next();
            String f[] = x.split(" ");
            String mx = f[1];
            if (mx.endsWith("."))
                mx = mx.substring(0, (mx.length() - 1));
            res.add(mx);
        }
        return res;
    }


    public static boolean isValid(String address) {
        // Find the separator for the domain name
        int pos = address.indexOf('@');
        // If the address does not contain an '@', it's not valid
        if (pos == -1) return false;
        // Isolate the domain/machine name and get a list of mail exchangers
        String domain = address.substring(++pos);
        List<String> mxList = null;
        try {
            mxList = getMX(domain);
        } catch (NamingException ex) {
            return false;
        }
        // Just because we can send mail to the domain, doesn't mean that the
        // address is valid, but if we can't, it's a sure sign that it isn't
        if (mxList.size() == 0) return false;


        // Now, do the SMTP validation, try each mail exchanger until we get
        // a positive acceptance. It *MAY* be possible for one MX to allow
        // a message [store and forwarder for example] and another [like
        // the actual mail server] to reject it. This is why we REALLY ought
        // to take the preference into account.
        for (String mx : mxList) {
            boolean valid = false;
            try (Socket socket = new Socket(mx, 25);
                 BufferedReader rdr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                 BufferedWriter wtr = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()))) {

                int res;

                res = hear(rdr);
                if (res != 220) throw new Exception("Invalid header");

                say(wtr, "EHLO sictiam.fr");
                res = hear(rdr);
                if (res != 250) throw new Exception("Not ESMTP");

                // validate the sender address
                say(wtr, "MAIL FROM: <stela@sictiam.fr>");
                res = hear(rdr);
                if (res != 250) throw new Exception("Sender rejected");

                say(wtr, "RCPT TO: <" + address + ">");
                res = hear(rdr);
                // be polite
                say(wtr, "RSET");
                hear(rdr);
                say(wtr, "QUIT");
                hear(rdr);
                if (res != 250) throw new Exception("Address is not valid!");

                return true;

            } catch (Exception ex) {
                LOGGER.error("Error from MX " + mx + " : " + ex.getMessage());
            }
        }
        return false;
    }
}