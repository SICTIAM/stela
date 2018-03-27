package fr.sictiam.stela.pesservice.model.migration;

public enum MigrationStatus {
    NOT_DONE("NOT_DONE"),
    ONGOING("ONGOING"),
    DONE("DONE");

    final String name;

    MigrationStatus(String s) {
        name = s;
    }

    @Override
    public String toString() {
        return this.name;
    }

}
