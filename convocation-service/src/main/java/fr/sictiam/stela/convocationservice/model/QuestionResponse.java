package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class QuestionResponse {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.QuestionResponseViewPublic.class)
    private String uuid;

    @JsonView(Views.QuestionResponseViewPublic.class)
    private Boolean response;

    @ManyToOne
    @JsonView(Views.QuestionResponseViewPublic.class)
    private Question question;

    @ManyToOne
    @JsonView(Views.QuestionResponseViewPrivate.class)
    private ConvocationResponse convocationResponse;

    public QuestionResponse() {
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
    }

    public Boolean getResponse() {
        return response;
    }

    public void setResponse(Boolean response) {
        this.response = response;
    }

    public String getUuid() {
        return uuid;
    }

    public ConvocationResponse getConvocationResponse() {
        return convocationResponse;
    }

    public void setConvocationResponse(ConvocationResponse convocationResponse) {
        this.convocationResponse = convocationResponse;
    }
}
