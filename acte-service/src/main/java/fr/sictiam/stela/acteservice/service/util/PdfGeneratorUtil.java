package fr.sictiam.stela.acteservice.service.util;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.html.simpleparser.HTMLWorker;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfCopy.PageStamp;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfSmartCopy;
import com.lowagie.text.pdf.PdfStamper;
import com.lowagie.text.pdf.PdfWriter;
import fr.sictiam.stela.acteservice.model.Thumbnail;
import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.tools.imageio.ImageIOUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class PdfGeneratorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfGeneratorUtil.class);

    @Autowired
    private TemplateEngine templateEngine;

    public byte[] createPdf(List<String> pages) throws DocumentException, IOException {
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
        if (StringUtils.isEmpty(templateName))
            throw new IllegalArgumentException("The templateName can not be empty/null");
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

    public Thumbnail getPDFThumbnail(byte[] pdf) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PDDocument document = PDDocument.load(pdf);

        PdfReader pdfReader = new PdfReader(pdf);
        Thumbnail.OrientationEnum orientationEnum;
        if (pdfIsRotated(pdfReader)) {
            orientationEnum = Thumbnail.OrientationEnum.LANDSCAPE;
        } else {
            orientationEnum = Thumbnail.OrientationEnum.PORTRAIT;
        }


        PDFRenderer pdfRenderer = new PDFRenderer(document);
        BufferedImage bim = pdfRenderer.renderImageWithDPI(0, 30, ImageType.RGB);
        ImageIOUtil.writeImage(bim, "png", baos, 30);
        document.close();

        String base64Image = Base64.encodeBase64String(baos.toByteArray());
        Thumbnail thumbnail = new Thumbnail(orientationEnum, base64Image);

        return thumbnail;
    }

    public byte[] mergePDFs(List<byte[]> pdfs) throws DocumentException, IOException {
        Document document = new Document();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfCopy copy = new PdfSmartCopy(document, baos);
        document.open();
        PageStamp stamp;
        PdfImportedPage page;
        PdfReader reader;
        for (byte[] pdf : pdfs) {
            reader = new PdfReader(pdf);
            int pageNumber = reader.getNumberOfPages();
            for (int i = 1; i <= pageNumber; i++) {
                document.newPage();
                page = copy.getImportedPage(reader, i);
                stamp = copy.createPageStamp(page);
                stamp.alterContents();
                copy.addPage(page);
            }
            reader.close();
        }
        document.close();
        return baos.toByteArray();
    }

    public byte[] stampPDF(String ARUuid, String ARDate, byte[] pdf, Integer percentPositionX, Integer percentPositionY)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);
        stamp.setRotateContents(false);

        Color color = new Color(43, 43, 43);
        Rectangle mediabox = reader.getBoxSize(1, "media");
        int pixelPositionX = 0;
        int pixelPositionY = 0;
        if (pdfIsRotated(reader)) {
            //rotate axes for PDF landscape
            pixelPositionX = Math.round((percentPositionY) * mediabox.getWidth() / 100);
            pixelPositionY = Math.round((percentPositionX) * mediabox.getHeight() / 100);
        } else {
            pixelPositionX = Math.round(percentPositionX * mediabox.getWidth() / 100);
            // Hack because the iTextPDF origin is at the lower-left, but the front is at
            // the top-left
            pixelPositionY = Math.round((100 - percentPositionY) * mediabox.getHeight() / 100) - 50;
        }


        int pageNumber = reader.getNumberOfPages();
        for (int i = 1; i <= pageNumber; i++) {
            PdfContentByte canvas = stamp.getOverContent(i);
            drawStampBorders(canvas, color, pixelPositionX, pixelPositionY);
            drawStampTitle(canvas, color, pixelPositionX, pixelPositionY);
            drawStampDetails(ARUuid, ARDate, canvas, color, pixelPositionX, pixelPositionY);
            canvas.stroke();
        }
        stamp.close();
        return baos.toByteArray();
    }

    private void drawStampTitle(PdfContentByte canvas, Color color, int pixelPositionX, int pixelPositionY)
            throws DocumentException {
        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(pixelPositionX + 65f, pixelPositionY + 20f, pixelPositionX + 200f, pixelPositionY + 45f);

        float fntSize = 12f;
        float lineSpacing = 10f;
        Font font = FontFactory.getFont(FontFactory.COURIER, fntSize, Font.BOLD, color);

        Paragraph p = new Paragraph(new Phrase(lineSpacing, "AR Prefecture", font));
        ct.addElement(p);
        ct.go();
    }

    private void drawStampDetails(String ARUuid, String ARDate, PdfContentByte canvas, Color color, int pixelPositionX,
                                  int pixelPositionY) throws DocumentException {
        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(pixelPositionX + 8f, pixelPositionY + 8f, pixelPositionX + 238f, pixelPositionY + 27f);

        float fntSize = 9f;
        float lineSpacing = 9f;
        Font font = FontFactory.getFont(FontFactory.COURIER, fntSize, color);

        Paragraph p1 = new Paragraph(new Phrase(lineSpacing, ARUuid, font));
        Paragraph p2 = new Paragraph(new Phrase(lineSpacing, "Reçu le " + ARDate, font));
        ct.addElement(p1);
        ct.addElement(p2);
        ct.go();
    }

    private void drawStampBorders(PdfContentByte canvas, Color color, int pixelPositionX, int pixelPositionY) {
        canvas.setColorStroke(color);

        // Draw thick outside line
        canvas.setLineWidth(3);
        canvas.moveTo(pixelPositionX, pixelPositionY);
        canvas.lineTo(pixelPositionX + 235, pixelPositionY);
        canvas.lineTo(pixelPositionX + 235, pixelPositionY + 50);
        canvas.lineTo(pixelPositionX, pixelPositionY + 50);
        canvas.closePathStroke();

        // Draw thin inside line
        canvas.setLineWidth(1);
        canvas.moveTo(pixelPositionX + 5, pixelPositionY + 5);
        canvas.lineTo(pixelPositionX + 230, pixelPositionY + 5);
        canvas.lineTo(pixelPositionX + 230, pixelPositionY + 30);
        canvas.lineTo(pixelPositionX + 5, pixelPositionY + 30);
        canvas.closePathStroke();
    }


    public boolean pdfIsRotated(PdfReader pdfReader){
        return pdfReader.getPageRotation(1) != 0 && pdfReader.getPageRotation(1) % 90 == 0;
    }

}
