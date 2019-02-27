package fr.sictiam.stela.convocationservice.service.util;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.service.LocalesService;
import org.springframework.stereotype.Service;

@Service
public abstract class DocumentGenerator {

    public enum Extension {
        pdf,
        csv
    }

    protected LocalesService localesService;

    protected DocumentGenerator() {
        localesService = new LocalesService();
    }

    public static DocumentGenerator of(Extension extension) {
        if (extension == Extension.csv)
            return new CsvDocumentGenerator();

        if (extension == Extension.pdf)
            return new PdfDocumentGenerator();

        throw new RuntimeException("Unable to handle " + extension + " extension");
    }

    public abstract byte[] generatePresenceList(Convocation convocation);
}