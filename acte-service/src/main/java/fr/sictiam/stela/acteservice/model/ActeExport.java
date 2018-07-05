package fr.sictiam.stela.acteservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.ZonedDateTime;

@Entity
public class ActeExport {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String acteUuid;
    private ZonedDateTime transmissionDateTime;
    private String fileName;
    private String filesNameList; //String with separator between each file name
    private String siren;
    private String department;
    private String district;
    private String agentName;
    private String agentFirstName;
    private String agentEmail;

    public ActeExport() {

    }

    public ActeExport(String acteUuid, ZonedDateTime transmissionDateTime, String fileName, String siren,
            String department, String district) {
        this.acteUuid = acteUuid;
        this.transmissionDateTime = transmissionDateTime;
        this.fileName = fileName;
        this.siren = siren;
        this.department = department;
        this.district = district;
    }

    public String getUuid() {
        return uuid;
    }

    public String getActeUuid() {
        return acteUuid;
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
}
