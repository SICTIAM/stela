package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Attachment;
import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Profile;
import fr.sictiam.stela.convocationservice.model.Question;
import fr.sictiam.stela.convocationservice.model.Recipient;

import java.time.LocalDateTime;
import java.util.Set;

public class ReceivedConvocationDetailUI extends ReceivedConvocationUI {

    protected Attachment attachment;

    protected Set<Attachment> annexes;

    protected Set<Question> questions;

    protected LocalDateTime sentDate;

    protected String location;

    protected String comment;

    protected Profile profile;

    public ReceivedConvocationDetailUI(Convocation convocation, Recipient recipient) {
        super(convocation, recipient);
        attachment = convocation.getAttachment();
        annexes = convocation.getAnnexes();
        questions = convocation.getQuestions();
        sentDate = convocation.getSentDate();
        location = convocation.getLocation();
        comment = convocation.getComment();
        profile = convocation.getProfile();
    }


    public Attachment getAttachment() {
        return attachment;
    }

    public Set<Attachment> getAnnexes() {
        return annexes;
    }

    public Set<Question> getQuestions() {
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
}
