package fr.sictiam.stela.pesservice.model;

import javax.persistence.*;
import java.util.List;

@Entity
public class LocalAuthority {

    @Id
    private String uuid;
    private String name;
    private String siren;
    private String siret;
    @Enumerated(EnumType.STRING)
    private ServerCode serverCode;
    private Boolean active;
    
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "sirens", joinColumns = @JoinColumn(name = "local_authority_uuid"))
    @Column(name = "siren")
    private List<String> sirens;
    
    public LocalAuthority() {
    }

    public LocalAuthority(String uuid, String name, String siren, Boolean active) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.active = active;
    }

    public LocalAuthority(String uuid, String name, String siren) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
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

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public String getSiret() {
        return siret;
    }

    public void setSiret(String siret) {
        this.siret = siret;
    }

    public ServerCode getServerCode() {
        return serverCode;
    }

    public void setServerCode(ServerCode serverCode) {
        this.serverCode = serverCode;
    }

    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public List<String> getSirens() {
        return sirens;
    }

    public void setSirens(List<String> sirens) {
        this.sirens = sirens;
    }

    @Override
    public String toString() {
        return "LocalAuthority [uuid=" + uuid + ", name=" + name + ", siren=" + siren + ", siret=" + siret
                + ", serverCode=" + serverCode + ", active=" + active + ", sirens=" + sirens + "]";
    }
}
