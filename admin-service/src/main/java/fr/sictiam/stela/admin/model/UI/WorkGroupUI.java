package fr.sictiam.stela.admin.model.UI;

import java.util.Set;

public class WorkGroupUI {

    private String uuid;

    private String name;

    private Set<String> rights;

    public WorkGroupUI() {
    }

    public String getUuid() {
        return uuid;
    }

    public Set<String> getRights() {
        return rights;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

}
