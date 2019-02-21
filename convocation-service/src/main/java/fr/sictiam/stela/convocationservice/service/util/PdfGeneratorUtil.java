package fr.sictiam.stela.convocationservice.service.util;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class PdfGeneratorUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PdfGeneratorUtil.class);

    private final LocalesService localesService;

    @Autowired
    public PdfGeneratorUtil(
            LocalesService localesService) {
        this.localesService = localesService;
    }

    public byte[] stampPDF(LocalDateTime date, byte[] pdf, Integer percentPositionX, Integer percentPositionY)
            throws IOException, DocumentException {
        PdfReader reader = new PdfReader(pdf);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfStamper stamp = new PdfStamper(reader, baos, '\0', true);
        stamp.setRotateContents(false);

        Color color = new Color(43, 43, 43);
        Rectangle mediabox = reader.getBoxSize(1, "media");

        int pixelPositionX = Math.round(percentPositionX * mediabox.getWidth() / 100);
        int pixelPositionY = 0;
        if (pdfIsRotated(reader)) {
            //rotate axes for PDF landscape
            pixelPositionY = Math.round((percentPositionX) * mediabox.getHeight() / 100);
        } else {
            // Hack because the iTextPDF origin is at the lower-left, but the front is at
            // the top-left
            pixelPositionY = Math.round((100 - percentPositionY) * mediabox.getHeight() / 100) - 50;
        }


        int pageNumber = reader.getNumberOfPages();
        for (int i = 1; i <= pageNumber; i++) {
            PdfContentByte canvas = stamp.getOverContent(i);
            drawStampBorders(canvas, color, pixelPositionX, pixelPositionY);
            drawStampDetails(canvas, color, pixelPositionX, pixelPositionY, date);
            canvas.stroke();
        }
        stamp.close();
        return baos.toByteArray();
    }

    private void drawStampDetails(PdfContentByte canvas, Color color, int pixelPositionX,
            int pixelPositionY, LocalDateTime date) throws DocumentException {
        ColumnText ct = new ColumnText(canvas);
        ct.setSimpleColumn(pixelPositionX + 8f, pixelPositionY + 15f, pixelPositionX + 158f, pixelPositionY + 35f);

        float fntSize = 9f;
        float lineSpacing = 9f;
        Font font = FontFactory.getFont(FontFactory.COURIER, fntSize, color);
        DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormat = DateTimeFormatter.ISO_LOCAL_TIME;
        LocalDateTime copy = LocalDateTime.of(date.toLocalDate(), date.toLocalTime().withNano(0));

        Map<String, String> params = new HashMap<String, String>() {
            {
                put("date", dateFormat.format(copy));
                put("time", timeFormat.format(copy));
            }
        };
        String stamp = localesService.getMessage("fr", "convocation", "$.convocation.stamp.title", params);
        Paragraph p = new Paragraph(new Phrase(lineSpacing, stamp, font));
        ct.addElement(p);
        ct.go();
    }

    private void drawStampBorders(PdfContentByte canvas, Color color, int pixelPositionX, int pixelPositionY) {
        canvas.setColorStroke(color);

        // Draw thick outside line
        canvas.setLineWidth(3);
        canvas.moveTo(pixelPositionX, pixelPositionY);
        canvas.lineTo(pixelPositionX + 155, pixelPositionY);
        canvas.lineTo(pixelPositionX + 155, pixelPositionY + 50);
        canvas.lineTo(pixelPositionX, pixelPositionY + 50);
        canvas.closePathStroke();

        // Draw thin inside line
        canvas.setLineWidth(1);
        canvas.moveTo(pixelPositionX + 5, pixelPositionY + 10);
        canvas.lineTo(pixelPositionX + 150, pixelPositionY + 10);
        canvas.lineTo(pixelPositionX + 150, pixelPositionY + 40);
        canvas.lineTo(pixelPositionX + 5, pixelPositionY + 40);
        canvas.closePathStroke();
    }


    public boolean pdfIsRotated(PdfReader pdfReader) {
        return pdfReader.getPageRotation(1) != 0 && pdfReader.getPageRotation(1) % 90 == 0;
    }

}
