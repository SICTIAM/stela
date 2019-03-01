package fr.sictiam.stela.convocationservice.service.util;

public class DocumentGeneratorFactory {

    public enum Extension {
        pdf,
        csv
    }

    public static DocumentGenerator of(Extension extension) {

        switch (extension) {
            case csv:
                return new CsvDocumentGenerator();
            case pdf:
                return new PdfDocumentGenerator();
            default:
                throw new RuntimeException("Unable to handle " + extension + " extension");
        }
    }
}
