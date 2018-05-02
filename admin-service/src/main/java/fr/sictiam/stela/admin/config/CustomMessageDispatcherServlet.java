package fr.sictiam.stela.admin.config;

import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.context.ApplicationContext;
import org.springframework.ws.transport.http.HttpTransportConstants;
import org.springframework.ws.transport.http.MessageDispatcherServlet;
import org.springframework.ws.wsdl.WsdlDefinition;

import javax.servlet.http.HttpServletRequest;

import java.util.Map;

public class CustomMessageDispatcherServlet extends MessageDispatcherServlet {

    /**
     *
     */
    private static final long serialVersionUID = 8165514777144053430L;
    private Map<String, WsdlDefinition> wsdlDefinitions;

    CustomMessageDispatcherServlet(ApplicationContext applicationContext) {

        super();
        setApplicationContext(applicationContext);
        setTransformWsdlLocations(true);
        setTransformSchemaLocations(false);
    }

    public CustomMessageDispatcherServlet() {
        super();
    }

    @Override
    protected void initStrategies(ApplicationContext context) {

        super.initStrategies(context);
        initWsdlDefinitions(context);
    }

    private void initWsdlDefinitions(ApplicationContext context) {

        wsdlDefinitions = BeanFactoryUtils.beansOfTypeIncludingAncestors(context, WsdlDefinition.class, true, false);
    }

    // here with dealing with "wsdl" parameter in HTTP GET request
    @Override
    protected WsdlDefinition getWsdlDefinition(HttpServletRequest request) {

        if (HttpTransportConstants.METHOD_GET.equals(request.getMethod())) {

            return wsdlDefinitions.get("paull_login");

        } else {
            return null;
        }
    }
}