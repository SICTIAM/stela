package fr.sictiam.stela.convocationservice.service.util;

import fr.sictiam.stela.convocationservice.model.Convocation;
import org.springframework.stereotype.Service;

@Service
public interface DocumentGenerator {

    enum Extension {
        pdf,
        csv
    }


    static DocumentGenerator of(Extension extension) {
        if (extension == Extension.csv)
            return new CsvDocumentGenerator();

        if (extension == Extension.pdf)
            return new PdfDocumentGenerator();

        throw new RuntimeException("Unable to handle " + extension + " extension");
    }

    byte[] generatePresenceList(Convocation convocation);
}