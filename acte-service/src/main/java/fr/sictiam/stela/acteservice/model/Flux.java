package fr.sictiam.stela.acteservice.model;

public enum Flux {
    TRANSMISSION_ACTE("1", "1"),
    COURRIER_SIMPLE("2", "1"),
    REPONSE_COURRIER_SIMPLE("2", "2"),
    DEMANDE_PIECE_COMPLEMENTAIRE("3", "1"),
    AR_PIECE_COMPLEMENTAIRE("3", "2"),
    REFUS_EXPLICITE_TRANSMISSION_PIECES_COMPLEMENTAIRES("3", "3"),
    TRANSMISSION_PIECES_COMPLEMENTAIRES("3", "4"),
    LETTRE_OBSERVATION("4", "1"),
    AR_LETTRE_OBSERVATION("4", "2"),
    REFUS_EXPLICITE_LETTRE_OBSERVATION("4", "3"),
    REPONSE_LETTRE_OBSEVATION("4", "4"),
    ANNULATION_TRANSMISSION("6", "1"),
    AR_ANNULATION_TRANSMISSION("6", "2");

    private String transactionNumber;
    private String fluxNumber;

    Flux(String transactionNumber, String fluxNumber) {
        this.transactionNumber = transactionNumber;
        this.fluxNumber = fluxNumber;
    }

    public String getTransactionNumber() {
        return this.transactionNumber;
    }

    public String getFluxNumber() {
        return this.fluxNumber;
    }
}