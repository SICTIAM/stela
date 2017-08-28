package fr.sictiam.stela.acteservice.config;

import org.apache.commons.beanutils.Converter;

import java.time.LocalDateTime;

public class LocalDateTimeConverter implements Converter {

    @Override
    public Object convert(Class type, Object value) {
        return LocalDateTime.parse((String) value);
    }
}
