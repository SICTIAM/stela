package fr.sictiam.stela.acteservice.service.util;

import com.lowagie.text.Document;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class PdfGeneratorUtil {

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] createPdf(List<String> pages) throws Exception {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter.getInstance(document, baos);

        document.open();
        HTMLWorker htmlWorker = new HTMLWorker(document);
        for (String page : pages) {
            htmlWorker.parse(new StringReader(page));
            document.newPage();
        }
        document.close();

        return baos.toByteArray();
    }

    public String getContentPage(String templateName, Map map) {
        if (StringUtils.isEmpty(templateName)) throw new IllegalArgumentException("The templateName can not be empty/null");
        Context ctx = new Context();
        if (map != null) {
            Iterator itMap = map.entrySet().iterator();
            while (itMap.hasNext()) {
                Map.Entry pair = (Map.Entry) itMap.next();
                ctx.setVariable(pair.getKey().toString(), pair.getValue());
            }
        }
        return templateEngine.process(templateName, ctx);
    }
}
