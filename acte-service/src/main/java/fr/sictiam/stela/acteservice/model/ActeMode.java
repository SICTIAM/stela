package fr.sictiam.stela.acteservice.model;

public enum ActeMode {
    ACTE ("ACTE"),
    ACTE_BUDGETAIRE ("ACTE_BUDGETAIRE"),
    ACTE_BATCH ("ACTE_BATCH");

    final String name;

    ActeMode(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }
}