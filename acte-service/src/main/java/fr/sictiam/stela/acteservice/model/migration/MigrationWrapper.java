package fr.sictiam.stela.acteservice.model.migration;

import java.util.List;
import java.util.Set;

public class MigrationWrapper {

    private List<UserMigration> userMigrations;
    private String moduleName;
    private Set<String> rights;

    public MigrationWrapper() {
    }

    public MigrationWrapper(List<UserMigration> userMigrations, String moduleName, Set<String> rights) {
        this.userMigrations = userMigrations;
        this.moduleName = moduleName;
        this.rights = rights;
    }

    public List<UserMigration> getUserMigrations() {
        return userMigrations;
    }

    public String getModuleName() {
        return moduleName;
    }

    public Set<String> getRights() {
        return rights;
    }
}