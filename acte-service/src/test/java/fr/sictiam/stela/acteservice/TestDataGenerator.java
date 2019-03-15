package fr.sictiam.stela.acteservice;

import fr.sictiam.stela.acteservice.model.Acte;
import fr.sictiam.stela.acteservice.model.ActeNature;
import fr.sictiam.stela.acteservice.model.Flux;
import fr.sictiam.stela.acteservice.model.StatusType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TestDataGenerator {

    public static Acte acte() {
        return new Acte("uuid", "Objet", LocalDateTime.now(), LocalDate.now(), "number",
                ActeNature.ARRETES_INDIVIDUELS, LocalDateTime.now(), StatusType.SENT, Flux.TRANSMISSION_ACTE);
    }
}
