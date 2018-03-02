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
    private ExternalUser externalUser;

    @JsonView(Views.ConvocationResponseViewPublic.class)
    private String substituteProfileUuid;

    @ManyToOne
    @JsonView(Views.ConvocationResponseViewPrivate.class)
    private ExternalUser substituteExternalUser;

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

    public ConvocationResponse(String profileUuid, ExternalUser externalUser, String substituteProfileUuid,
            ExternalUser substituteExternalUser, Convocation convocation, ResponseType responseType,
            Set<QuestionResponse> questionResponses) {
        this.profileUuid = profileUuid;
        this.externalUser = externalUser;
        this.substituteProfileUuid = substituteProfileUuid;
        this.substituteExternalUser = substituteExternalUser;
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

    public ExternalUser getExternalUser() {
        return externalUser;
    }

    public void setExternalUser(ExternalUser externalUser) {
        this.externalUser = externalUser;
    }

    public String getSubstituteProfileUuid() {
        return substituteProfileUuid;
    }

    public void setSubstituteProfileUuid(String substituteProfileUuid) {
        this.substituteProfileUuid = substituteProfileUuid;
    }

    public ExternalUser getSubstituteExternalUser() {
        return substituteExternalUser;
    }

    public void setSubstituteExternalUser(ExternalUser substituteExternalUser) {
        this.substituteExternalUser = substituteExternalUser;
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
