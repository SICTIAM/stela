package fr.sictiam.stela.pesservice.model;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import fr.sictiam.stela.pesservice.config.LocalDateTimeDeserializer;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import java.time.LocalDateTime;

@Entity
public class PendingMessage {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String pesUuid;

    public PendingMessage() {
    }

    public PendingMessage(PesHistory pesHistory) {
        this.pesUuid = pesHistory.getPesUuid();
    }

    public String getPesUuid() {
        return pesUuid;
    }

}
