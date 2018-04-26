package fr.sictiam.stela.acteservice.model.migration;

public class MigrationLog {

    private String logs;

    public MigrationLog() {
        this.logs = "";
    }

    public String getLogs() {
        return logs;
    }

    public void setLogs(String logs) {
        this.logs = logs;
    }
}
