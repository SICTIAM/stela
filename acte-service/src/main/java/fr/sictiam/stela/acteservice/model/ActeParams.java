package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateDeserializer;
import org.apache.tomcat.jni.Local;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ActeParams {

    @NotNull
    @Size(max = 15)
    @Pattern(regexp = "^[A-Z0-9_]+$")
    private String number;

    @NotNull
    @Size(max = 500)
    private String objet;

    @NotNull
    private ActeNature nature;

    @NotNull
    @Pattern(regexp = "^[0-9]-[0-9]-[0-9]-[0-9]-[0-9]$")
    private String code;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    @NotNull
    private LocalDate decision;

    @NotNull
    private MultipartFile file;

    @NotNull
    @NotBlank
    String fileType;

    private List<MultipartFile> annexes;

    private List<String> annexeTypes;

    private boolean isPublic = false;

    private boolean isPublicWebsite = false;

    @Pattern(regexp = "^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$")
    private String groupUuid;

    @Email
    private String email;

    @NotNull
    private LocalAuthority localAuthority;

    public ActeParams() {
    }

    public ActeParams(@NotNull @Size(max = 15) @Pattern(regexp = "^[A-Z0-9_]+$") String number, @NotNull @Size(max = 500) String objet, @NotNull ActeNature nature, @NotNull @Pattern(regexp = "^[0-9]-[0-9]-[0-9]-[0-9]-[0-9]$") String code, @NotNull LocalDate decision, @NotNull MultipartFile file, @NotBlank String fileType, List<MultipartFile> annexes, List<String> annexeTypes, boolean isPublic, boolean isPublicWebsite, @Pattern(regexp = "^[a-z0-9]{8}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{4}-[a-z0-9]{12}$") String groupUuid, String email, @NotNull LocalAuthority localAuthority) {
        this.number = number;
        this.objet = objet;
        this.nature = nature;
        this.code = code;
        this.decision = decision;
        this.file = file;
        this.fileType = fileType;
        this.annexes = annexes;
        this.annexeTypes = annexeTypes;
        this.isPublic = isPublic;
        this.isPublicWebsite = isPublicWebsite;
        this.groupUuid = groupUuid;
        this.email = email;
        this.localAuthority = localAuthority;
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public LocalDate getDecision() {
        return decision;
    }

    public void setDecision(LocalDate decision) {
        this.decision = decision;
    }

    public ActeNature getNature() {
        return nature;
    }

    public void setNature(ActeNature nature) {
        this.nature = nature;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getObjet() {
        return objet;
    }

    public void setObjet(String objet) {
        this.objet = objet;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public boolean isPublicWebsite() {
        return isPublicWebsite;
    }

    public void setPublicWebsite(boolean isPublicWebsite) {
        this.isPublicWebsite = isPublicWebsite;
    }

    public MultipartFile getFile() {
        return file;
    }

    public void setFile(MultipartFile file) {
        this.file = file;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public List<MultipartFile> getAnnexes() {
        return annexes != null ? annexes : new ArrayList<>();
    }

    public void setAnnexes(List<MultipartFile> annexes) {
        this.annexes = annexes;
    }

    public List<String> getAnnexeTypes() {
        return annexeTypes;
    }

    public void setAnnexeTypes(List<String> annexeTypes) {
        this.annexeTypes = annexeTypes;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGroupUuid() {
        return groupUuid;
    }

    public void setGroupUuid(String groupUuid) {
        this.groupUuid = groupUuid;
    }

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public void setLocalAuthority(LocalAuthority localAuthority) {
        this.localAuthority = localAuthority;
    }

    @Override
    public String toString() {
        return "ActeParams{" + "number:" + number + '\'' + ", decision:" + decision + ", nature:" + nature
                + ", code:'" + code + '\'' + ", objet:'" + objet + '\'' + ", isPublic:" + isPublic
                + ", isPublicWebsite:" + isPublicWebsite + ", file:'" + file.getOriginalFilename()
                + "', fileType:'" + fileType + "', annexes:["
                + annexes.stream().map(a -> a.getOriginalFilename()).collect(Collectors.joining(","))
                + "], annexeTypes:[" + annexeTypes.stream().collect(Collectors.joining(","))
                + "], groupUuid:'" + groupUuid + "', email:'" + email + "'}";
    }
}
