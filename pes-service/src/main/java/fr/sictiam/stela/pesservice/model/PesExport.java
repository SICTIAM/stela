package fr.sictiam.stela.pesservice.model;

import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.ZonedDateTime;

@Entity
public class PesExport {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String pesUuid;
    private ZonedDateTime transmissionDateTime;
    private String fileName;
    private long size;
    private String sha1;
    private String siren;
    private String agentName;
    private String agentFirstName;
    private String agentEmail;

    public PesExport() {
    }

    public PesExport(String pesUuid, ZonedDateTime transmissionDateTime, String fileName, long size, String sha1,
            String siren) {
        this.pesUuid = pesUuid;
        this.transmissionDateTime = transmissionDateTime;
        this.fileName = fileName;
        this.size = size;
        this.sha1 = sha1;
        this.siren = siren;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPesUuid() {
        return pesUuid;
    }

    public void setPesUuid(String pesUuid) {
        this.pesUuid = pesUuid;
    }

    public ZonedDateTime getTransmissionDateTime() {
        return transmissionDateTime;
    }

    public void setTransmissionDateTime(ZonedDateTime transmissionDateTime) {
        this.transmissionDateTime = transmissionDateTime;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getSha1() {
        return sha1;
    }

    public void setSha1(String sha1) {
        this.sha1 = sha1;
    }

    public String getSiren() {
        return siren;
    }

    public void setSiren(String siren) {
        this.siren = siren;
    }

    public String getAgentName() {
        return agentName;
    }

    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }

    public String getAgentFirstName() {
        return agentFirstName;
    }

    public void setAgentFirstName(String agentFirstName) {
        this.agentFirstName = agentFirstName;
    }

    public String getAgentEmail() {
        return agentEmail;
    }

    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }
}
