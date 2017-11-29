package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;

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
    private LocalDate nomenclatureDate;
    private byte[] nomenclatureFile;
    private Boolean canPublishRegistre;
    private Boolean canPublishWebSite;
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private StampPosition stampPosition;

    public LocalAuthority() {
    }

    public LocalAuthority(String name, String siren, String department, String district, String nature) {
        this.name = name;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.nature = nature;
        this.canPublishRegistre = false;
        this.canPublishWebSite = false;
    }

    public LocalAuthority(String name, String siren, String department, String district, String nature,
                          Boolean canPublishRegistre, Boolean canPublishWebSite) {
        this.name = name;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.nature = nature;
        this.canPublishRegistre = canPublishRegistre;
        this.canPublishWebSite = canPublishWebSite;
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

    public LocalDate getNomenclatureDate() {
        return nomenclatureDate;
    }

    public void setNomenclatureDate(LocalDate nomenclatureDate) {
        this.nomenclatureDate = nomenclatureDate;
    }

    public byte[] getNomenclatureFile() {
        return nomenclatureFile;
    }

    public void setNomenclatureFile(byte[] nomenclatureFile) {
        this.nomenclatureFile = nomenclatureFile;
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

    public StampPosition getStampPosition() {
        return stampPosition;
    }

    public void setStampPosition(StampPosition stampPosition) {
        this.stampPosition = stampPosition;
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
