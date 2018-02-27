package fr.sictiam.stela.acteservice.model.event;

import fr.sictiam.stela.acteservice.model.ActeHistory;
import fr.sictiam.stela.acteservice.model.Attachment;
import org.springframework.context.ApplicationEvent;

import java.util.List;

public class ActeHistoryEvent extends ApplicationEvent {

    private ActeHistory acteHistory;

    public ActeHistoryEvent(Object source, ActeHistory acteHistory) {
        super(source);
        this.acteHistory = acteHistory;
    }

    public ActeHistoryEvent(Object source, ActeHistory acteHistory, List<Attachment> attachments) {
        super(source);
        this.acteHistory = acteHistory;
        this.attachments = attachments;
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
