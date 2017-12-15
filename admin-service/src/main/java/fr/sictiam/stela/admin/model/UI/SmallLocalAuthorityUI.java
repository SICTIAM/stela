package fr.sictiam.stela.admin.model.UI;

import fr.sictiam.stela.admin.model.LocalAuthority;

public class SmallLocalAuthorityUI {

    private String uuid;
    private String name;
    private String siren;

    public SmallLocalAuthorityUI(LocalAuthority localAuthority) {
        this.uuid = localAuthority.getUuid();
        this.name = localAuthority.getName();
        this.siren = localAuthority.getSiren();
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public String getSiren() {
        return siren;
    }
}
