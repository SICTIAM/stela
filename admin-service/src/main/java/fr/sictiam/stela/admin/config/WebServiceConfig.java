package fr.sictiam.stela.admin.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.wsdl.wsdl11.DefaultWsdl11Definition;
import org.springframework.xml.xsd.SimpleXsdSchema;
import org.springframework.xml.xsd.XsdSchema;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    private static final String NAMESPACE_URI = "http://www.processmaker.com";

    @Bean
    public ServletRegistrationBean<CustomMessageDispatcherServlet> messageDispatcherServletPaull(
            ApplicationContext applicationContext) {
        CustomMessageDispatcherServlet servlet = new CustomMessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        return new ServletRegistrationBean<CustomMessageDispatcherServlet>(servlet, "/externalws/*");
    }

    @Bean(name = "paull_login")
    public DefaultWsdl11Definition defaultWsdl11Definition(XsdSchema paullSchema) {
        DefaultWsdl11Definition wsdl11Definition = new DefaultWsdl11Definition();
        wsdl11Definition.setPortTypeName("PaullLogin");
        wsdl11Definition.setLocationUri("/fr/classic/services/wsdl2");
        wsdl11Definition.setTargetNamespace(NAMESPACE_URI);
        wsdl11Definition.setSchema(paullSchema);
        return wsdl11Definition;
    }

    @Bean
    @Primary
    public XsdSchema paullSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/paull.xsd"));
    }
}