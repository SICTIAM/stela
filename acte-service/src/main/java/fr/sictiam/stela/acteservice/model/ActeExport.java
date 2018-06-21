package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.acteservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.ZonedDateTime;
import java.util.Objects;

@Entity
public class ActeExport {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private ZonedDateTime transmissionDateTime;
    private String fileName;
    private String filesNameList; //String with separator between each file name
    private String siren;
    private String department;
    private String district;
    private String agentName;
    private String agentFirstName;
    private String agentEmail;

    public  ActeExport() {

    }

    public ActeExport(ZonedDateTime transmissionDateTime, String fileName, String filesNameList, String siren, String department, String district, String agentName, String agentFirstName, String agentEmail) {
        this.uuid = uuid;
        this.transmissionDateTime = transmissionDateTime;
        this.fileName = fileName;
        this.filesNameList = filesNameList;
        this.siren = siren;
        this.department = department;
        this.district = district;
        this.agentName = agentName;
        this.agentFirstName = agentFirstName;
        this.agentEmail = agentEmail;
    }

    public String getUuid() {
        return uuid;
    }

    public ZonedDateTime getTransmissionDateTime() {
        return transmissionDateTime;
    }

    public String getFileName() {
        return fileName;
    }

    public String getFilesNameList() {
        return filesNameList;
    }

    public String getSiren() {
        return siren;
    }

    public String getDepartment() {
        return department;
    }

    public String getDistrict() {
        return district;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getAgentFirstName() {
        return agentFirstName;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setTransmissionDateTime(ZonedDateTime transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setFilesNameList(String filesNameList) {
        this.filesNameList = filesNameList;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public void setAgentFirstName(String agentFirstName) {
        this.agentFirstName = agentFirstName;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }

    @Override
    public boolean equals(Object that){
        if(this == that) return true;//if both of them points the same address in memory

        if(!(that instanceof ActeExport)) return false; // if "that" is not an ActeExport or a childclass

        ActeExport thatActeExport = (ActeExport) that; // than we can cast it to People safely

        return this.transmissionDateTime.equals(thatActeExport.getTransmissionDateTime()) &&
                this.fileName.equals(thatActeExport.getFileName()) &&
                this.filesNameList.equals(thatActeExport.getFilesNameList()) &&
                this.siren.equals(thatActeExport.getSiren()) &&
                this.department.equals(thatActeExport.getDepartment()) &&
                this.district.equals(thatActeExport.getDistrict()) &&
                this.agentName.equals(thatActeExport.getAgentName())&&
                this.agentFirstName.equals(thatActeExport.getAgentFirstName()) &&
                this.agentEmail.equals(thatActeExport.getAgentEmail());
    }

    @Override
    public int hashCode() {
        return Objects.hash(transmissionDateTime, fileName, filesNameList, siren, department, district, agentName, agentFirstName, agentEmail);
    }
}
