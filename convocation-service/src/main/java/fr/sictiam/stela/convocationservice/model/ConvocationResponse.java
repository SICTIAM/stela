package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;

import java.util.Set;

@Entity
public class ConvocationResponse {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.ConvocationResponseViewPublic.class)
    private String uuid;

    @JsonView(Views.ConvocationResponseViewPublic.class)
    private String profileUuid;

    @ManyToOne
    @JsonView(Views.ConvocationResponseViewPrivate.class)
    private Recipient recipient;

    @JsonView(Views.ConvocationResponseViewPublic.class)
    private String substituteProfileUuid;

    @ManyToOne
    @JsonView(Views.ConvocationResponseViewPrivate.class)
    private Recipient substituteRecipient;

    @ManyToOne
    @JsonView(Views.ConvocationResponseViewPrivate.class)
    private Convocation convocation;

    @Enumerated(EnumType.STRING)
    @JsonView(Views.ConvocationResponseViewPublic.class)
    private ResponseType responseType;

    @OneToMany
    @JsonView(Views.ConvocationResponseViewPrivate.class)
    private Set<QuestionResponse> questionResponses;

    public ConvocationResponse() {
    }

    public ConvocationResponse(String profileUuid, Recipient recipient, String substituteProfileUuid,
            Recipient substituteRecipient, Convocation convocation, ResponseType responseType,
            Set<QuestionResponse> questionResponses) {
        this.profileUuid = profileUuid;
        this.recipient = recipient;
        this.substituteProfileUuid = substituteProfileUuid;
        this.substituteRecipient = substituteRecipient;
        this.convocation = convocation;
        this.responseType = responseType;
        this.questionResponses = questionResponses;
    }

    public String getProfileUuid() {
        return profileUuid;
    }

    public void setProfileUuid(String profileUuid) {
        this.profileUuid = profileUuid;
    }

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    public String getSubstituteProfileUuid() {
        return substituteProfileUuid;
    }

    public void setSubstituteProfileUuid(String substituteProfileUuid) {
        this.substituteProfileUuid = substituteProfileUuid;
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

    public Set<QuestionResponse> getQuestionResponses() {
        return questionResponses;
    }

    public void setQuestionResponses(Set<QuestionResponse> questionResponses) {
        this.questionResponses = questionResponses;
    }

    public String getUuid() {
        return uuid;
    }
}
