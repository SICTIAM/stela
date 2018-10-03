package fr.sictiam.stela.pesservice.model.event;

import fr.sictiam.stela.pesservice.model.LocalAuthority;

import java.util.Set;

public class LocalAuthorityEvent extends Event {

    private String uuid;
    private String name;
    private String siren;
    private String slugName;

    private Set<String> activatedModules;

    public LocalAuthorityEvent() {
        super(LocalAuthorityEvent.class.getName());
    }

    public LocalAuthorityEvent(LocalAuthority localAuthority) {
        super(LocalAuthorityEvent.class.getName());
        this.uuid = localAuthority.getUuid();
        this.name = localAuthority.getName();
        this.siren = localAuthority.getSiren();
        this.slugName = localAuthority.getSlugName();
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public String getSlugName() {
        return slugName;
    }

    public void setSlugName(String slugName) {
        this.slugName = slugName;
    }

    public Set<String> getActivatedModules() {
        return activatedModules;
    }

    public void setActivatedModules(Set<String> activatedModules) {
        this.activatedModules = activatedModules;
    }

}
