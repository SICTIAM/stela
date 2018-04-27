package fr.sictiam.stela.acteservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class AttachmentType {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String code;

    private String label;

    @ManyToOne
    @JsonIgnore
    private AttachmentTypeReferencial attachmentTypeReferencial;

    public AttachmentType() {
    }

    public AttachmentType(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public AttachmentTypeReferencial getAttachmentTypeReferencial() {
        return attachmentTypeReferencial;
    }

    public void setAttachmentTypeReferencial(AttachmentTypeReferencial attachmentTypeReferencial) {
        this.attachmentTypeReferencial = attachmentTypeReferencial;
    }
}
