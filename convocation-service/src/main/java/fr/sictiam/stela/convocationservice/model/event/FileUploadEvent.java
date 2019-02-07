package fr.sictiam.stela.convocationservice.model.event;

import fr.sictiam.stela.convocationservice.model.Attachment;
import org.springframework.context.ApplicationEvent;

public class FileUploadEvent extends ApplicationEvent {

    private Attachment attachment;

    public FileUploadEvent(Object source, Attachment attachment) {
        super(source);
        this.attachment = attachment;
    }

    public Attachment getAttachment() {
        return attachment;
    }
}
