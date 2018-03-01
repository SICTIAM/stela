package fr.sictiam.stela.convocationservice.model;

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
    private String uuid;

    private Boolean response;

    @ManyToOne
    private Question question;

    @ManyToOne
    private ConvocationResponse convocationResponse;

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
