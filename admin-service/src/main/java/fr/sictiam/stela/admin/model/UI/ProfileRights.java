package fr.sictiam.stela.admin.model.UI;

import java.util.List;

public class ProfileRights {

    private List<String> groupUuids;
    private boolean admin;

    public ProfileRights() {
    }

    public List<String> getGroupUuids() {
        return groupUuids;
    }

    public boolean isAdmin() {
        return admin;
    }
}
