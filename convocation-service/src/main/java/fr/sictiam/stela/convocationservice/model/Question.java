package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;

import java.util.Set;

@Entity
public class Question {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.QuestionView.class)
    private String uuid;

    private String convocationUuid;

    @JsonView(Views.QuestionView.class)
    private String question;

    @OneToMany
    private Set<QuestionResponse> responses;

    public Question() {
    }

    public Question(String question) {
        this.question = question;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getUuid() {
        return uuid;
    }

    public String getConvocationUuid() {
        return convocationUuid;
    }

    public void setConvocationUuid(String convocationUuid) {
        this.convocationUuid = convocationUuid;
    }
}
