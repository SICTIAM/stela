package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.validation.constraints.NotNull;

@Entity
public class QuestionResponse implements Comparable<QuestionResponse> {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Public.class)
    private String uuid;

    @JsonView(Views.Public.class)
    private Boolean response;

    @ManyToOne
    private Question question;

    @OneToOne
    @JsonView(Views.Public.class)
    private Recipient recipient;

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

    public Recipient getRecipient() {
        return recipient;
    }

    public void setRecipient(Recipient recipient) {
        this.recipient = recipient;
    }

    @Override public int compareTo(@NotNull QuestionResponse questionResponse) {
        return recipient.compareTo(questionResponse.getRecipient());
    }

    @Override public String toString() {
        return '{' +
                "\"uuid\": \"" + uuid + "\"" +
                ", \"response\": " + response +
                ", \"recipient\": " + recipient +
                '}';
    }
}
