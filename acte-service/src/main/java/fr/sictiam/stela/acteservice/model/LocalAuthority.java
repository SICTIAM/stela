package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;

@Entity
public class LocalAuthority {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String name;
    private String siren;
    private String department;
    private String district;
    private String nature;
    private LocalDateTime nomenclatureDate;
    private Boolean canPublishRegistre;
    private Boolean canPublishWebSite;

    public LocalAuthority() {
    }

    public LocalAuthority(String name, String siren, String department, String district, String nature) {
        this.name = name;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.nature = nature;
        this.nomenclatureDate = null;
        this.canPublishRegistre = false;
        this.canPublishWebSite = false;
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

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getNature() {
        return nature;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public LocalDateTime getNomenclatureDate() {
        return nomenclatureDate;
    }

    public void setNomenclatureDate(LocalDateTime nomenclatureDate) {
        this.nomenclatureDate = nomenclatureDate;
    }

    public Boolean getCanPublishRegistre() {
        return canPublishRegistre;
    }

    public void setCanPublishRegistre(Boolean canPublishRegistre) {
        this.canPublishRegistre = canPublishRegistre;
    }

    public Boolean getCanPublishWebSite() {
        return canPublishWebSite;
    }

    public void setCanPublishWebSite(Boolean canPublishWebSite) {
        this.canPublishWebSite = canPublishWebSite;
    }

    @Override
    public String toString() {
        return "LocalAuthority{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", siren='" + siren + '\'' +
                ", department='" + department + '\'' +
                ", district='" + district + '\'' +
                ", nature='" + nature + '\'' +
                ", nomenclatureDate=" + nomenclatureDate +
                ", canPublishRegistre=" + canPublishRegistre +
                ", canPublishWebSite=" + canPublishWebSite +
                '}';
    }
}
