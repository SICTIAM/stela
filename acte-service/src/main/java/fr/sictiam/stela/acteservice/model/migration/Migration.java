package fr.sictiam.stela.acteservice.model.migration;

import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

@Embeddable
public class Migration {

    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationUsers;
    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationData;
    @Enumerated(EnumType.STRING)
    private MigrationStatus migrationUsersDeactivation;

    public Migration() {
        migrationUsers = MigrationStatus.NOT_DONE;
        migrationData = MigrationStatus.NOT_DONE;
        migrationUsersDeactivation = MigrationStatus.NOT_DONE;
    }

    public MigrationStatus getMigrationUsers() {
        return migrationUsers;
    }

    public void setMigrationUsers(MigrationStatus migrationUsers) {
        this.migrationUsers = migrationUsers;
    }

    public MigrationStatus getMigrationData() {
        return migrationData;
    }

    public void setMigrationData(MigrationStatus migrationData) {
        this.migrationData = migrationData;
    }

    public MigrationStatus getMigrationUsersDeactivation() {
        return migrationUsersDeactivation;
    }

    public void setMigrationUsersDeactivation(MigrationStatus migrationUsersDeactivation) {
        this.migrationUsersDeactivation = migrationUsersDeactivation;
    }
}
