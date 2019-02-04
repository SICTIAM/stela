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

    public QuestionResponse() {
    }

    public QuestionResponse(Boolean response, Question question) {
        this.response = response;
        this.question = question;
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
}
