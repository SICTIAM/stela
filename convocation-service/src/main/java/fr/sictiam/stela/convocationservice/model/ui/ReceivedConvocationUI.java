package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.ResponseType;

import java.time.LocalDateTime;
import java.util.Optional;

public class ReceivedConvocationUI {

    protected String uuid;

    protected String subject;

    protected LocalDateTime meetingDate;

    protected ResponseType response;

    protected String assemblyType;

    protected boolean opened;

    protected boolean cancelled;

    protected LocalDateTime cancellationDate;

    public ReceivedConvocationUI(Convocation convocation, Recipient recipient) {
        uuid = convocation.getUuid();
        subject = convocation.getSubject();
        meetingDate = convocation.getMeetingDate();
        assemblyType = convocation.getAssemblyType().getName();

        Optional<RecipientResponse> opt =
                convocation.getRecipientResponses()
                        .stream().filter(recipientResponse -> recipientResponse.getRecipient().equals(recipient)).findFirst();

        response = opt.isPresent() ? opt.get().getResponseType() : ResponseType.DO_NOT_KNOW;
        opened = opt.isPresent() && opt.get().isOpened();

        cancelled = convocation.isCancelled();
        cancellationDate = convocation.getCancellationDate();
    }

    public String getUuid() {
        return uuid;
    }

    public String getSubject() {
        return subject;
    }

    public LocalDateTime getMeetingDate() {
        return meetingDate;
    }

    public String getAssemblyType() {
        return assemblyType;
    }

    public ResponseType getResponse() {
        return response;
    }

    public boolean isOpened() {
        return opened;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public LocalDateTime getCancellationDate() {
        return cancellationDate;
    }
}
