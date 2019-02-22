package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Question;
import fr.sictiam.stela.convocationservice.model.Recipient;

public class QuestionUI {

    private String uuid;

    private String question;

    private Boolean response;

    private Integer rank;

    public QuestionUI(Question question, Recipient recipient) {
        uuid = question.getUuid();
        this.question = question.getQuestion();
        rank = question.getRank();

        // extract current recipient response if exists
        question.getResponses().stream().filter(qr -> qr.getRecipient().equals(recipient)).findFirst().ifPresent(qr -> response = qr.getResponse());
    }

    public String getUuid() {
        return uuid;
    }

    public String getQuestion() {
        return question;
    }

    public Boolean getResponse() {
        return response;
    }

    public Integer getRank() {
        return rank;
    }
}
