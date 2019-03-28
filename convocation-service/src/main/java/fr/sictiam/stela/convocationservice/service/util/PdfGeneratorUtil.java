package fr.sictiam.stela.convocationservice.service.util;


import com.itextpdf.text.BaseColor;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

        BaseColor color = new BaseColor(43, 43, 43);
        com.itextpdf.text.Rectangle mediabox = reader.getBoxSize(1, "media");

        int pixelPositionX = Math.round(percentPositionX * mediabox.getWidth() / 100);
        int pixelPositionY = 0;
        if (pdfIsLandscape(reader)) {
            if (isNativeLandscape(reader)) {
                //landscape document
                pixelPositionX = Math.round((percentPositionX) * mediabox.getWidth() / 100);
                pixelPositionY = Math.round((100 - percentPositionY) * mediabox.getHeight() / 100) - 50;
            } else {
                //PDF portrait doc which had been rotated to Landscape
                pixelPositionX = Math.round((percentPositionX) * mediabox.getHeight() / 100);
                pixelPositionY = Math.round((100 - percentPositionY) * mediabox.getWidth() / 100) - 50;

            }
        } else {
            if (!isNativeLandscape(reader)) {
                // Hack because the iTextPDF origin is at the lower-left, but the front is at
                // the top-leff
                pixelPositionX = Math.round(percentPositionX * mediabox.getWidth() / 100);
                pixelPositionY = Math.round((100 - percentPositionY) * mediabox.getHeight() / 100) - 50;
            } else {
                //PDF Landscape doc which had been rotated to Portrait
                pixelPositionX = Math.round(percentPositionX * mediabox.getHeight() / 100);
                pixelPositionY = Math.round((100 - percentPositionY) * mediabox.getWidth() / 100) - 50;
            }
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

    private void drawStampDetails(PdfContentByte canvas, BaseColor color, int pixelPositionX,
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

    private void drawStampBorders(PdfContentByte canvas, BaseColor color, int pixelPositionX, int pixelPositionY) {
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


    public boolean pdfIsLandscape(PdfReader pdfReader) {
        Rectangle mediabox = pdfReader.getBoxSize(1, "media");
        float width = mediabox.getWidth();
        float height = mediabox.getHeight();
        int rot = pdfReader.getPageRotation(1);
        boolean orientationHasChanged = rot != 0 && (rot % 90 == 0 && (rot / 90) % 2 != 0);

        if (isNativeLandscape(pdfReader)) {
            return !orientationHasChanged;
        } else {
            return orientationHasChanged;
        }
    }

    public boolean isNativeLandscape(PdfReader pdfReader) {
        Rectangle mediabox = pdfReader.getBoxSize(1, "media");
        float width = mediabox.getWidth();
        float height = mediabox.getHeight();
        return width > height;
    }


}
