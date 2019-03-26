package fr.sictiam.stela.convocationservice.service.util;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import com.lowagie.text.Cell;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.ResponseType;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

public class PdfDocumentGenerator implements DocumentGenerator {

    private final static Logger LOGGER = LoggerFactory.getLogger(PdfDocumentGenerator.class);

    private LocalesService localesService;

    public PdfDocumentGenerator() {
        localesService = new LocalesService();
    }

    @Override public byte[] generatePresenceList(Convocation convocation) {

        Document document = new Document();
        try {
            LocalDateTime copy = LocalDateTime.of(convocation.getMeetingDate().toLocalDate(),
                    convocation.getMeetingDate().toLocalTime().withNano(0));
            DateTimeFormatter dateFormat = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH");
            DateTimeFormatter minuteFormat = DateTimeFormatter.ofPattern("mm");
            Map<String, String> params = new HashMap<String, String>() {
                {
                    put("title", convocation.getSubject());
                    put("date", dateFormat.format(copy));
                    put("hours", hourFormat.format(copy));
                    put("minutes", minuteFormat.format(copy));
                }
            };

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            document.open();

            Paragraph title = new Paragraph();
            title.setAlignment(Element.ALIGN_CENTER);
            title.getFont().setSize(16f);
            title.add(localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.title", params));
            document.add(title);


            Paragraph dateText = new Paragraph();
            dateText.setAlignment(Element.ALIGN_CENTER);
            dateText.add(localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.meetingDate",
                    params));
            document.add(dateText);

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            /*
             * present recipients table
             */
            Paragraph present = new Paragraph();
            present.getFont().setSize(14f);
            present.getFont().setStyle(Font.UNDERLINE);
            present.add(localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.presentList",
                    params));
            document.add(present);
            document.add(Chunk.NEWLINE);

            PdfPTable presentTable = new PdfPTable(4);
            presentTable.setWidthPercentage(100);
            Stream.of(localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.lastname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.firstname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.localAuthority"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.signature")).forEach(columnTitle -> {
                PdfPCell header = generateCell(columnTitle, BaseColor.LIGHT_GRAY);
                presentTable.addCell(header);
            });
            convocation.getRecipientResponses().stream().filter(recipientResponse -> recipientResponse.getResponseType() == ResponseType.PRESENT && !recipientResponse.isGuest()).forEach(recipientResponse -> {
                presentTable.addCell(generateCell(recipientResponse.getRecipient().getLastname()));
                presentTable.addCell(generateCell(recipientResponse.getRecipient().getFirstname()));
                presentTable.addCell(generateCell(StringUtils.isNotBlank(recipientResponse.getRecipient().getEpciName())
                        ? recipientResponse.getRecipient().getEpciName()
                        : ""));
                presentTable.addCell(generateCell(""));
            });
            document.add(presentTable);
            document.newPage();

            /*
             * not present recipients table
             */
            Paragraph notPresent = new Paragraph();
            notPresent.getFont().setSize(14f);
            notPresent.getFont().setStyle(Font.UNDERLINE);
            notPresent.add(localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.notPresentList",
                    params));
            document.add(notPresent);
            document.add(Chunk.NEWLINE);

            PdfPTable notPresentTable = new PdfPTable(3);
            notPresentTable.setWidthPercentage(100);
            Stream.of(localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.lastname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.firstname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.localAuthority")).forEach(columnTitle -> {
                PdfPCell header = generateCell(columnTitle, BaseColor.LIGHT_GRAY);
                notPresentTable.addCell(header);
            });
            convocation.getRecipientResponses().stream().filter(recipientResponse -> recipientResponse.getResponseType() == ResponseType.NOT_PRESENT && !recipientResponse.isGuest()).forEach(recipientResponse -> {
                notPresentTable.addCell(generateCell(recipientResponse.getRecipient().getLastname()));
                notPresentTable.addCell(generateCell(recipientResponse.getRecipient().getFirstname()));
                notPresentTable.addCell(generateCell(""));
            });
            document.add(notPresentTable);
            document.newPage();

            /*
             * guests table
             */
            Paragraph guests = new Paragraph();
            guests.getFont().setSize(14f);
            guests.getFont().setStyle(Font.UNDERLINE);
            guests.add(localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.guestList",
                    params));
            document.add(guests);
            document.add(Chunk.NEWLINE);

            PdfPTable guestTable = new PdfPTable(4);
            guestTable.setWidthPercentage(100);
            Stream.of(localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.lastname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.admin.modules.convocation" +
                            ".recipient_config.firstname"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.pdf.localAuthority"),
                    localesService.getMessage("fr", "convocation", "$.convocation.export.presence")).forEach(columnTitle -> {
                PdfPCell header = generateCell(columnTitle, BaseColor.LIGHT_GRAY);
                guestTable.addCell(header);
            });
            convocation.getRecipientResponses().stream().filter(recipientResponse -> recipientResponse.isGuest()).forEach(recipientResponse -> {
                guestTable.addCell(generateCell(recipientResponse.getRecipient().getLastname()));
                guestTable.addCell(generateCell(recipientResponse.getRecipient().getFirstname()));
                guestTable.addCell(generateCell(""));
                guestTable.addCell(generateCell(localesService.getMessage("fr", "convocation",
                        "$.convocation.export." + recipientResponse.getResponseType())));
            });
            document.add(guestTable);

            document.close();
            writer.close();
            return baos.toByteArray();
        } catch (DocumentException e) {
            LOGGER.error("Error while generating PDF presence list: {}", e.getMessage());
            return new byte[0];
        }
    }

    private PdfPCell generateCell(String text) {
        return generateCell(text, BaseColor.WHITE);
    }

    private PdfPCell generateCell(String text, BaseColor color) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(color);
        cell.setPadding(6f);
        cell.setHorizontalAlignment(Cell.ALIGN_LEFT);
        cell.setVerticalAlignment(Cell.ALIGN_MIDDLE);
        cell.setBorderWidth(1);
        cell.setPhrase(new Phrase(text));
        return cell;
    }
}