package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.LocalAuthority;
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

    protected LocalAuthority localAuthority;

    protected RecipientUI substitute;

    protected boolean guest;

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
        substitute = opt.isPresent() && opt.get().getResponseType() == ResponseType.SUBSTITUTED ?
                new RecipientUI(opt.get().getSubstituteRecipient()) : null;
        guest = opt.isPresent() && opt.get().isGuest();

        cancelled = convocation.isCancelled();
        cancellationDate = convocation.getCancellationDate();
        localAuthority = convocation.getLocalAuthority();
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

    public LocalAuthority getLocalAuthority() {
        return localAuthority;
    }

    public RecipientUI getSubstitute() {
        return substitute;
    }

    public boolean isGuest() {
        return guest;
    }
}
