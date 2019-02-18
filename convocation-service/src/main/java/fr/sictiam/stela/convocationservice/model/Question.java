package fr.sictiam.stela.convocationservice.model;

import com.fasterxml.jackson.annotation.JsonView;
import fr.sictiam.stela.convocationservice.model.ui.Views;
import fr.sictiam.stela.convocationservice.model.util.QuestionResponseComparator;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.SortComparator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.Collectors;

@Entity
public class Question {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @JsonView(Views.Question.class)
    private String uuid;

    @JsonView(Views.Question.class)
    private String question;

    @OneToMany(mappedBy = "question", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @SortComparator(QuestionResponseComparator.class)
    @JsonView(Views.Public.class)
    private SortedSet<QuestionResponse> responses;

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

    public SortedSet<QuestionResponse> getResponses() {
        if (responses == null)
            responses = new TreeSet<>();
        return responses;
    }

    public void setResponses(SortedSet<QuestionResponse> responses) {
        this.responses = responses;
    }

    @Override public String toString() {
        return "{ \"uuid\": \"" + uuid + "\"" +
                ", \"question\": \"" + question + "\"" +
                ", \"responses\": [" + getResponses().stream().map(QuestionResponse::toString).collect(Collectors.joining(",")) + "]}";

    }
}
