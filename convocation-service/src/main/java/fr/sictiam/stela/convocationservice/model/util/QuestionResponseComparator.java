package fr.sictiam.stela.convocationservice.model.util;

import fr.sictiam.stela.convocationservice.model.QuestionResponse;

import java.util.Comparator;

public class QuestionResponseComparator implements Comparator<QuestionResponse> {
    @Override
    public int compare(QuestionResponse o1, QuestionResponse o2) {
        String rr1 = o1.getRecipient().getLastname() + o1.getRecipient().getFirstname() + o1.getRecipient().getEmail();
        String rr2 = o2.getRecipient().getLastname() + o2.getRecipient().getFirstname() + o2.getRecipient().getEmail();
        return rr1.compareTo(rr2);
    }
}