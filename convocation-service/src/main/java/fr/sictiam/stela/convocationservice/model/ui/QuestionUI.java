package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Question;
import fr.sictiam.stela.convocationservice.model.Recipient;

public class QuestionUI {

    private String uuid;

    private String question;

    private Boolean response;

    public QuestionUI(Question question, Recipient recipient) {
        uuid = question.getUuid();
        this.question = question.getQuestion();

        // extract current recipient response if exists
        question.getResponses().stream().filter(qr -> qr.getRecipient().equals(recipient)).findFirst().ifPresent(qr -> this.response = qr.getResponse());
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
}
