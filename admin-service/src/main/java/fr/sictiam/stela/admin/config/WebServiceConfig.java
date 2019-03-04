package fr.sictiam.stela.admin.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.LocationTransformerObjectSupport;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;
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
        servlet.setWsdlDefinitionHandlerAdapterBeanName("paull_location");
        return new ServletRegistrationBean<>(servlet, "/externalws/*");
    }

    @Bean(name = "paull_location")
    public LocationTransformerObjectSupport customLocationTransformer() {
        return new CustomLocationTransformer();
    }

    @Bean(name = "paull_login")
    public Wsdl11Definition defaultWsdl11Definition() {
        SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("/wsdl/ws-paull.wsdl"));
        return wsdl11Definition;
    }

    @Bean
    @Primary
    public XsdSchema paullSchema() {
        return new SimpleXsdSchema(new ClassPathResource("wsdl/paull.xsd"));
    }
}