package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.WorkGroup;

public class WorkGroupUI {

    private String uuid;
    private String name;
    private SmallLocalAuthorityUI localAuthority;

    public WorkGroupUI(WorkGroup workGroup) {
        this.uuid = workGroup.getUuid();
        this.name = workGroup.getName();
        this.localAuthority = new SmallLocalAuthorityUI(workGroup.getLocalAuthority());
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public SmallLocalAuthorityUI getLocalAuthority() {
        return localAuthority;
    }
}
