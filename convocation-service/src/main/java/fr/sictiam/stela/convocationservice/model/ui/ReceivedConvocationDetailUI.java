package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.model.Recipient;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ReceivedConvocationDetailUI extends ReceivedConvocationUI {

    protected Attachment attachment;

    protected Attachment procuration;

    protected Set<Attachment> annexes;

    protected List<QuestionUI> questions;

    protected LocalDateTime sentDate;

    protected String location;

    protected String comment;

    protected Profile profile;

    protected boolean useProcuration;

    protected List<RecipientResponseUI> recipients;

    public ReceivedConvocationDetailUI(Convocation convocation, Recipient recipient) {
        super(convocation, recipient);
        attachment = convocation.getAttachment();
        procuration = convocation.getProcuration();
        annexes = convocation.getAnnexes();
        sentDate = convocation.getSentDate();
        location = convocation.getLocation();
        comment = convocation.getComment();
        profile = convocation.getProfile();
        useProcuration = convocation.getAssemblyType().getUseProcuration();

        questions =
                convocation.getQuestions().stream().map(question -> new QuestionUI(question, recipient)).collect(Collectors.toList());

        recipients =
                convocation.getRecipientResponses()
                        .stream()
                        .filter(recipientResponse -> !recipientResponse.getRecipient().equals(recipient) && !recipientResponse.isGuest())
                        .map(recipientResponse -> new RecipientResponseUI(recipientResponse))
                        .collect(Collectors.toList());
    }


    public Attachment getAttachment() {
        return attachment;
    }

    public Attachment getProcuration() {
        return procuration;
    }

    public Set<Attachment> getAnnexes() {
        return annexes;
    }

    public List<QuestionUI> getQuestions() {
        return questions;
    }

    public LocalDateTime getSentDate() {
        return sentDate;
    }

    public String getLocation() {
        return location;
    }

    public String getComment() {
        return comment;
    }

    public Profile getProfile() {
        return profile;
    }

    public boolean isUseProcuration() {
        return useProcuration;
    }

    public List<RecipientResponseUI> getRecipients() {
        return recipients;
    }
}
