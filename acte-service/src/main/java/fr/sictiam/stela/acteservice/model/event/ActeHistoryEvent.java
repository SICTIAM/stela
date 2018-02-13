package fr.sictiam.stela.acteservice.model.event;

import java.util.List;

import org.springframework.context.ApplicationEvent;

import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Attachment;

public class ActeHistoryEvent extends ApplicationEvent {

    private ActeHistory acteHistory;

    public ActeHistoryEvent(Object source, ActeHistory acteHistory) {
        super(source);
        this.acteHistory = acteHistory;
    }

    // transient attachments
    private List<Attachment> attachments;

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this.attachments = attachments;
    }

    public ActeHistory getActeHistory() {
        return acteHistory;
    }
}
