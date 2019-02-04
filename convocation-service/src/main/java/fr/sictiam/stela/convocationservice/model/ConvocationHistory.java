package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.convocationservice.config.LocalDateTimeDeserializer;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;
import org.jetbrains.annotations.NotNull;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import java.time.LocalDateTime;

@Entity
public class ConvocationHistory implements Comparable<ConvocationHistory> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Convocation.class)
    private String uuid;

    @ManyToOne
    @JsonView(Views.ConvocationInternal.class)
    private Convocation convocation;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.Convocation.class)
    private StatusType status;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime date;

    @Column(length = 1024)
    @JsonView(Views.Convocation.class)
    private String message;

    @OneToOne
    @JsonView(Views.Convocation.class)
    private Attachment attachment;

    public ConvocationHistory() {
    }

    public ConvocationHistory(Convocation convocation, StatusType status) {
        this.convocation = convocation;
        this.status = status;
        date = LocalDateTime.now();
    }

    public ConvocationHistory(Convocation convocation, StatusType status, LocalDateTime date, String message) {
        this.convocation = convocation;
        this.status = status;
        this.date = date;
        this.message = message;
    }

    public ConvocationHistory(Convocation convocation, StatusType status, LocalDateTime date, byte[] file, String fileName) {
        this.convocation = convocation;
        this.status = status;
        this.date = date;
    }

    public ConvocationHistory(Convocation convocation, StatusType status, LocalDateTime date, byte[] file, String fileName,
            String message) {
        this.convocation = convocation;
        this.status = status;
        this.date = date;
        this.message = message;
    }

    public String getUuid() {
        return uuid;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public StatusType getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Attachment getAttachment() {
        return attachment;
    }

    public void setAttachment(Attachment attachment) {
        this.attachment = attachment;
    }


    @Override
    public String toString() {
        return "ConvocationHistory{" + "uuid='" + uuid + '\'' + ", convocationUuid='" + convocation.getUuid() + '\'' + ", " +
                "status="
                + status + ", date=" + date + ", message='" + message + '\'' + '}';
    }

    @Override public int compareTo(@NotNull ConvocationHistory convocationHistory) {
        return date.compareTo(convocationHistory.getDate());
    }
}
