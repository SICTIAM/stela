package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import java.time.LocalDateTime;

@Entity
public class RecipientResponse implements Comparable<RecipientResponse> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Public.class)
    private String uuid;

    @ManyToOne
    @JsonView(Views.Recipient.class)
    private Recipient recipient;

    @ManyToOne
    @JsonView(Views.Recipient.class)
    private Recipient substituteRecipient;

    @ManyToOne
    @JsonIgnore
    private Convocation convocation;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.Public.class)
    private ResponseType responseType = ResponseType.DO_NOT_KNOW;

    @JsonView(Views.Public.class)
    private boolean opened = false;

    @JsonView(Views.Public.class)
    private LocalDateTime openDate;

    @JsonView(Views.Recipient.class)
    private boolean guest = false;

    public RecipientResponse() {
    }

    public RecipientResponse(Recipient recipient) {
        this.recipient = recipient;
    }

    public RecipientResponse(Recipient recipient, Convocation convocation) {
        this.recipient = recipient;
        this.convocation = convocation;
        guest = recipient.isGuest();
    }

    public RecipientResponse(Recipient recipient, Recipient substituteRecipient, Convocation convocation,
            ResponseType responseType) {
        this.recipient = recipient;
        this.substituteRecipient = substituteRecipient;
        this.convocation = convocation;
        this.responseType = responseType;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public Recipient getSubstituteRecipient() {
        return substituteRecipient;
    }

    public void setSubstituteRecipient(Recipient substituteRecipient) {
        this.substituteRecipient = substituteRecipient;
    }

    public Convocation getConvocation() {
        return convocation;
    }

    public void setConvocation(Convocation convocation) {
        this.convocation = convocation;
    }

    public ResponseType getResponseType() {
        return responseType;
    }

    public void setResponseType(ResponseType responseType) {
        this.responseType = responseType;
    }

    public boolean isOpened() {
        return opened;
    }

    public void setOpened(boolean opened) {
        this.opened = opened;
    }

    public LocalDateTime getOpenDate() {
        return openDate;
    }

    public void setOpenDate(LocalDateTime openDate) {
        this.openDate = openDate;
    }

    public String getUuid() {
        return uuid;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean guest) {
        this.guest = guest;
    }

    @Override public int compareTo(@NotNull RecipientResponse recipientResponse) {
        return recipient.compareTo(recipientResponse.getRecipient());
    }
}
