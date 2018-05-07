package fr.sictiam.stela.admin.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.admin.model.UI.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;

import java.util.Set;

@Entity
public class GenericAccount {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.GenericAccountView.class)
    private String uuid;

    @JsonView(Views.GenericAccountView.class)
    private String software;

    @JsonView(Views.GenericAccountView.class)
    private String email;

    @JsonView(Views.GenericAccountView.class)
    private String password;

    @JsonView(Views.GenericAccountView.class)
    private String serial;

    @JsonView(Views.GenericAccountView.class)
    private String vendor;

    @ManyToMany(targetEntity = LocalAuthority.class, fetch = FetchType.EAGER)
    @JoinTable(name = "generic_account_local_authorities", joinColumns = {
            @JoinColumn(name = "generic_account_uuid") }, inverseJoinColumns = {
                    @JoinColumn(name = "local_authority_uuid") })
    @JsonView(Views.GenericAccountView.class)
    private Set<LocalAuthority> localAuthorities;

    public GenericAccount() {
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getSerial() {
        return serial;
    }

    public void setSerial(String serial) {
        this.serial = serial;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public Set<LocalAuthority> getLocalAuthorities() {
        return localAuthorities;
    }

    public void setLocalAuthorities(Set<LocalAuthority> localAuthorities) {
        this.localAuthorities = localAuthorities;
    }

}
