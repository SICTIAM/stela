package fr.sictiam.stela.acteservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import java.nio.charset.StandardCharsets;
import java.util.Collections;

@Configuration
public class XmlSchemaConfig {

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setPackagesToScan("fr.sictiam.stela.acteservice.model.xml");
        jaxb2Marshaller.setSchema(new ClassPathResource("schemas/actesv1_1.xsd"));
        jaxb2Marshaller.setMarshallerProperties(Collections.singletonMap(Marshaller.JAXB_ENCODING, StandardCharsets.ISO_8859_1.name()));
        return jaxb2Marshaller;
    }
}
