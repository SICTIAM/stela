package fr.sictiam.stela.pesservice.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.ws.config.annotation.EnableWs;
import org.springframework.ws.config.annotation.WsConfigurerAdapter;
import org.springframework.ws.transport.http.LocationTransformerObjectSupport;
import org.springframework.ws.wsdl.wsdl11.SimpleWsdl11Definition;
import org.springframework.ws.wsdl.wsdl11.Wsdl11Definition;

@EnableWs
@Configuration
public class WebServiceConfig extends WsConfigurerAdapter {

    @Bean
    public ServletRegistrationBean<CustomMessageDispatcherServlet> messageDispatcherServlet(
            ApplicationContext applicationContext) {
        CustomMessageDispatcherServlet servlet = new CustomMessageDispatcherServlet();
        servlet.setApplicationContext(applicationContext);
        servlet.setTransformWsdlLocations(true);
        servlet.setWsdlDefinitionHandlerAdapterBeanName("paull_location");
        return new ServletRegistrationBean<>(servlet, "/api/pes/ws/*", "/externalws/*");
    }

    @Bean(name = "paull_location")
    public LocationTransformerObjectSupport customLocationTransformer() {
        return new CustomLocationTransformer();
    }

    @Bean(name = "wshelios")
    public Wsdl11Definition defaultWsdl11Definition() {
        SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("/wsdl/ws-helios.wsdl"));
        return wsdl11Definition;
    }

    @Bean(name = "wshelios_paul")
    public Wsdl11Definition defaultWsdl11DefinitionPaull() {
        SimpleWsdl11Definition wsdl11Definition = new SimpleWsdl11Definition();
        wsdl11Definition.setWsdl(new ClassPathResource("/wsdl/ws-paull.wsdl"));
        return wsdl11Definition;
    }
}