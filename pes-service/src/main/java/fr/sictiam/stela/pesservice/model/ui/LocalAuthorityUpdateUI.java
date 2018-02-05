package fr.sictiam.stela.pesservice.model.ui;

import fr.sictiam.stela.pesservice.model.ServerCode;
import fr.sictiam.stela.pesservice.validation.SirenCollection;

import java.util.List;

public class LocalAuthorityUpdateUI {

    private ServerCode serverCode;
    @SirenCollection
    private List<String> sirens;

    public LocalAuthorityUpdateUI() {
    }

    public ServerCode getServerCode() {
        return serverCode;
    }

    public List<String> getSirens() {
        return sirens;
    }
}
