package fr.sictiam.stela.acteservice.model;

import fr.sictiam.stela.acteservice.model.migration.Migration;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.List;

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
    // Default deposit profile for external software
    private String genericProfileUuid;
    private Migration migration;
    private ArchiveSettings archiveSettings;

    @Embedded
    private StampPosition stampPosition;

    @OneToMany(mappedBy = "localAuthority")
    private List<MaterialCode> materialCodes;

    @OneToMany(mappedBy = "localAuthority", cascade = CascadeType.ALL)
    private List<AttachmentTypeReferencial> attachmentTypeReferencials;

    public LocalAuthority() {
    }

    // uiid is generated only once in AdminService
    public LocalAuthority(String uuid, String name, String siren, String department, String district, String nature) {
        this.uuid = uuid;
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
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.nature = nature;
        this.canPublishRegistre = canPublishRegistre;
        this.canPublishWebSite = canPublishWebSite;
    }

    public LocalAuthority(String uuid, String name, String siren, Boolean active) {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.active = active;
    }

    public LocalAuthority(String uuid, String name, String siren) throws IOException {
        this.uuid = uuid;
        this.name = name;
        this.siren = siren;
        this.nomenclatureDate = LocalDate.of(2001, 1, 1);

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("examples/exemple_codes_matieres.xml");

        byte[] targetArray = new byte[in.available()];
        in.read(targetArray);
        this.nomenclatureFile = targetArray;
        this.canPublishRegistre = false;
        this.canPublishWebSite = false;
        this.setStampPosition(new StampPosition(10, 10));
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

    public List<AttachmentTypeReferencial> getAttachmentTypeReferencials() {
        return attachmentTypeReferencials;
    }

    public void setAttachmentTypeReferencials(List<AttachmentTypeReferencial> attachmentTypeReferencials) {
        this.attachmentTypeReferencials = attachmentTypeReferencials;
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

    public String getGenericProfileUuid() {
        return genericProfileUuid;
    }

    public void setGenericProfileUuid(String genericProfileUuid) {
        this.genericProfileUuid = genericProfileUuid;
    }

    public Migration getMigration() {
        return migration;
    }

    public void setMigration(Migration migration) {
        this.migration = migration;
    }

    public ArchiveSettings getArchiveSettings() {
        return archiveSettings;
    }

    public void setArchiveSettings(ArchiveSettings archiveSettings) {
        this.archiveSettings = archiveSettings;
    }

    @Override
    public String toString() {
        return "LocalAuthority{" + "uuid='" + uuid + '\'' + ", name='" + name + '\'' + ", siren='" + siren + '\''
                + ", department='" + department + '\'' + ", district='" + district + '\'' + ", nature='" + nature + '\''
                + ", nomenclatureDate=" + nomenclatureDate + ", canPublishRegistre=" + canPublishRegistre
                + ", canPublishWebSite=" + canPublishWebSite + '}';
    }
}
