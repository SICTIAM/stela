package fr.sictiam.stela.acteservice.model;

import java.io.IOException;
import java.util.Map;

import org.codehaus.groovy.control.CompilationFailedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;

/**
 * Base class for all xml files needed in transaction with minister.
 */
public abstract class XmlBase {

    private static final Logger LOGGER = LoggerFactory.getLogger(XmlBase.class);

    /**
     * Groovy template file used to create the xml file.
     */
    private String templateFile;

    protected void setTemplateFile(String templateFile){
        this.templateFile = templateFile;
    }

    /**
     * Create a xml file based on template and context.
     * 
     * @return The xml file content into a String.
     */
    public String toXmlString() {
        String result = "";
        Map<String, Object> context = createContext();

        TemplateEngine engine = new SimpleTemplateEngine();

        try {
            ClassPathResource cpr = new ClassPathResource(this.templateFile);
            Template template = engine.createTemplate(cpr.getFile());
            result = template.make(context).toString();
        } catch(CompilationFailedException|ClassNotFoundException|IOException e) {
            LOGGER.error("Unable to create file from template {} : {}", this.templateFile, e);
        }

        return result;
    }

    protected abstract Map<String, Object> createContext();
}