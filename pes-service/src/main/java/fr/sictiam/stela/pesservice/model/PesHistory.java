package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
public class PesHistory implements Comparable<PesHistory> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String pesUuid;
    @Enumerated(EnumType.STRING)
    private StatusType status;
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime date;
    @JsonIgnore
    private byte[] file;
    private String fileName;
    // Error messages can be quite lengthy
    @Column(length = 1024)
    @JsonIgnore
    private String message;
    @Type(type="com.vladmihalcea.hibernate.type.json.JsonBinaryType")
    List<PesHistoryError> errors;

    public PesHistory() {
    }

    public PesHistory(String pesUuid, StatusType status) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = LocalDateTime.now();
    }

    public PesHistory(String pesUuid, StatusType status, List<PesHistoryError> errors) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = LocalDateTime.now();
        this.errors = errors;
    }

    public PesHistory(String pesUuid, StatusType status, String error) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = LocalDateTime.now();
        if (!StringUtils.isEmpty(error)) {
            this.errors = new ArrayList<>();
            errors.add(new PesHistoryError("", error));
        }
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, List<PesHistoryError> errors) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.errors = errors;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, String error) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        if (!StringUtils.isEmpty(error)) {
            this.errors = new ArrayList<>();
            errors.add(new PesHistoryError("", error));
        }
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, byte[] file, String fileName) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, byte[] file, String fileName,
                      List<PesHistoryError> errors) {
        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
        this.errors = errors;
    }

    public PesHistory(String pesUuid, StatusType status, LocalDateTime date, byte[] file, String fileName,
                      String error) {

        this.pesUuid = pesUuid;
        this.status = status;
        this.date = date;
        this.file = file;
        this.fileName = fileName;
        if (!StringUtils.isEmpty(error)) {
            this.errors = new ArrayList<>();
            errors.add(new PesHistoryError("", error));
        }
    }

    public String getMessage () {
        return message;
    }

    public void setMessage (String message) {
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public String getPesUuid() {
        return pesUuid;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public StatusType getStatus() {
        return status;
    }

    public byte[] getFile() {
        return file;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFile(byte[] file) {
        this.file = file;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public List<PesHistoryError> getErrors () {
        return errors;
    }

    public void setErrors (List<PesHistoryError> errors) {
        this.errors = errors;
    }

    public void addError (PesHistoryError error) {
        errors.add(error);
    }

    @Override
    public int compareTo(PesHistory acteHistory) {
        return this.date.compareTo(acteHistory.getDate());
    }

    @Override
    public String toString() {
        return "ActeHistory{" + "uuid='" + uuid + '\'' + ", pesUuid='" + pesUuid + '\'' + ", status=" + status
                + ", date=" + date + ", fileName='" + fileName + '\'' + '}';
    }
}
