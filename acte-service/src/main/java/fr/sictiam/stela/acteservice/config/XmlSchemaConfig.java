package fr.sictiam.stela.acteservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

@Configuration
public class XmlSchemaConfig {

    @Bean
    public Jaxb2Marshaller jaxb2Marshaller() {
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setPackagesToScan("fr.sictiam.stela.acteservice.model.xml");
        jaxb2Marshaller.setSchema(new ClassPathResource("schemas/actesv1_1.xsd"));
        return jaxb2Marshaller;
    }
}
