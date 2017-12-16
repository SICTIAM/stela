package fr.sictiam.stela.acteservice.model;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class LocalAuthority {

    @Id
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
    private Boolean active;

    @Embedded
    private StampPosition stampPosition;
    
    @OneToMany(mappedBy="localAuthority")
    private List<MaterialCode> materialCodes;
    
    @OneToMany(mappedBy = "localAuthority", fetch = FetchType.EAGER ,cascade= CascadeType.ALL, orphanRemoval=true)
    private Set<WorkGroup> groups;
    
    @OneToMany(mappedBy = "localAuthority", fetch = FetchType.EAGER ,cascade= CascadeType.ALL, orphanRemoval=true)
    private Set<Profile> profiles;
    
    public LocalAuthority() {
    }
    //uiid is generated only once in AdminService
    public LocalAuthority(String uuid, String name, String siren, String department, String district, String nature) {
        this.uuid=uuid;
        this.name = name;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.nature = nature;
        this.canPublishRegistre = false;
        this.canPublishWebSite = false;
    }

    public LocalAuthority(String uuid, String name, String siren, String department, String district, String nature,
                          Boolean canPublishRegistre, Boolean canPublishWebSite) {
        this.uuid=uuid;
        this.name = name;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.nature = nature;
        this.canPublishRegistre = canPublishRegistre;
        this.canPublishWebSite = canPublishWebSite;
    }
    
    public LocalAuthority(String uuid, String name, String siren, Boolean active, Set<WorkGroup> groups,
            Set<Profile> profiles) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.active = active;
        this.groups = groups;
        this.profiles = profiles;
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
    
    public List<MaterialCode> getMaterialCodes() {
        return materialCodes;
    }

    public void setMaterialCodes(List<MaterialCode> materialCodes) {
        this.materialCodes = materialCodes;
    }

    public StampPosition getStampPosition() {
        return stampPosition;
    }

    public void setStampPosition(StampPosition stampPosition) {
        this.stampPosition = stampPosition;
    }
    
    public Boolean isActive() {
        return active;
    }
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Set<WorkGroup> getGroups() {
        return groups != null ? groups : new HashSet<>();
    }
    public void setGroups(Set<WorkGroup> groups) {
        this.groups = groups;
    }
    public Set<Profile> getProfiles() {
        return profiles != null ? profiles : new HashSet<>();
    }
    public void setProfiles(Set<Profile> profiles) {
        this.profiles = profiles;
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
