package fr.sictiam.stela.convocationservice.model.ui;

import fr.sictiam.stela.convocationservice.model.Convocation;
import fr.sictiam.stela.convocationservice.model.Recipient;
import fr.sictiam.stela.convocationservice.model.RecipientResponse;
import fr.sictiam.stela.convocationservice.model.ResponseType;

import java.time.LocalDateTime;
import java.util.Optional;

public class ReceivedConvocationUI {

    private String uuid;

    private String subject;

    private LocalDateTime meetingDate;

    private ResponseType response;

    private String assemblyType;

    private boolean opened;

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
}
