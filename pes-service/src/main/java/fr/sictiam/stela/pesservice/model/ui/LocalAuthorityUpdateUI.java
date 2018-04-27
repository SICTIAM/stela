package fr.sictiam.stela.pesservice.model.ui;

import fr.sictiam.stela.pesservice.model.ServerCode;
import fr.sictiam.stela.pesservice.validation.SirenCollection;

import java.util.List;

public class LocalAuthorityUpdateUI {

    private ServerCode serverCode;
    @SirenCollection
    private List<String> sirens;

    private Boolean sesileSubscription;
    private String token;
    private String secret;
    private String genericProfileUuid;

    public LocalAuthorityUpdateUI() {
    }

    public ServerCode getServerCode() {
        return serverCode;
    }

    public List<String> getSirens() {
        return sirens;
    }

    public Boolean getSesileSubscription() {
        return sesileSubscription;
    }

    public String getToken() {
        return token;
    }

    public String getSecret() {
        return secret;
    }

    public String getGenericProfileUuid() {
        return genericProfileUuid;
    }

    public void setGenericProfileUuid(String genericProfileUuid) {
        this.genericProfileUuid = genericProfileUuid;
    }
}
