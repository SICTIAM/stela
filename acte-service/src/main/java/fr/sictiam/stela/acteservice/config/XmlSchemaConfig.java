package fr.sictiam.stela.acteservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.Marshaller;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class XmlSchemaConfig {

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setPackagesToScan("fr.sictiam.stela.acteservice.model.xml");
        jaxb2Marshaller.setSchema(new ClassPathResource("schemas/actesv1_1.xsd"));
        Map<String, Object> marshallerProperties = new HashMap<String, Object>() {{
            put(Marshaller.JAXB_ENCODING, "ISO-8859-1");
            put(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        }};
        jaxb2Marshaller.setMarshallerProperties(marshallerProperties);
        return jaxb2Marshaller;
    }
}
