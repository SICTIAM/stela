package fr.sictiam.signature.utils;

import fr.sictiam.signature.pes.verifier.UnExpectedException;

import javax.xml.bind.DatatypeConverter;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;

import java.util.GregorianCalendar;

public class DateUtils {
    public static String utcFormat(GregorianCalendar calendar) {
        if (calendar != null) {
            return DatatypeConverter.printDateTime(calendar);
        }
        return null;
    }

    public static GregorianCalendar parseUtc(String utcDateString) {
        if (utcDateString != null) {
            try {
                return DatatypeFactory.newInstance().newXMLGregorianCalendar(utcDateString).toGregorianCalendar();
            } catch (DatatypeConfigurationException ex) {
                throw new UnExpectedException(ex);
            }
        }
        return null;
    }

    public static boolean isStrictUtcFormat(String utcDateString) {
        try {
            parseUtc(utcDateString);
            return (utcDateString != null) && (utcDateString.endsWith("Z"));
        } catch (IllegalArgumentException iae) {
        }
        return false;
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.utils.DateUtils Java Class
 * Version: 6 (50.0) JD-Core Version: 0.7.1
 */