package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import java.time.LocalDateTime;

@Entity
public class ConvocationHistory implements Comparable<ConvocationHistory> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Convocation.class)
    private String uuid;

    @ManyToOne
    @JoinColumn(name = "convocation_uuid")
    @JsonIgnore
    private Convocation convocation;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.Convocation.class)
    private HistoryType type;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonView(Views.Convocation.class)
    private LocalDateTime date;

    @Column(length = 1024)
    @JsonView(Views.Convocation.class)
    private String message;

    @JsonView(Views.Convocation.class)
    private boolean publicHistory = true;

    public ConvocationHistory() {
    }

    public ConvocationHistory(Convocation convocation, HistoryType type) {
        this(convocation, type, "", false);
    }

    public ConvocationHistory(Convocation convocation, HistoryType type, String message, boolean publicHistory) {
        this.convocation = convocation;
        this.type = type;
        this.message = message;
        this.publicHistory = publicHistory;
        date = LocalDateTime.now();
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

    public HistoryType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isPublicHistory() {
        return publicHistory;
    }

    @Override
    public String toString() {
        return "{" + "uuid='" + uuid + '\'' + ", convocationUuid='" + convocation.getUuid() + '\'' + ", " +
                "type="
                + type + ", date=" + date + ", message='" + message + '\'' + '}';
    }

    @Override public int compareTo(@NotNull ConvocationHistory convocationHistory) {
        return convocationHistory.getDate().compareTo(date);
    }
}
