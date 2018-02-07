package fr.sictiam.stela.acteservice.model;

public enum Flux {
    TRANSMISSION_ACTE("1", "1"),
    REPONSE_COURRIER_SIMPLE("2", "2"),
    REFUS_EXPLICITE_TRANSMISSION_PIECES_COMPLEMENTAIRES("3", "3"),
    TRANSMISSION_PIECES_COMPLEMENTAIRES("3", "4"),
    REFUS_EXPLICITE_LETTRE_OBSERVATION("4", "3"),
    REPONSE_LETTRE_OBSEVATION("4", "4"),
    ANNULATION_TRANSMISSION("6", "1"),
    DEMANDE_CLASSIFICATION("7", "1");

    private String transactionNumber;
    private String fluxNumber;

    Flux(String transactionNumber, String fluxNumber) {
        this.transactionNumber = transactionNumber;
        this.fluxNumber = fluxNumber;
    }

    public String getTransactionNumber(){
        return this.transactionNumber;
    }

    public String getFluxNumber(){
        return this.fluxNumber;
    }
}