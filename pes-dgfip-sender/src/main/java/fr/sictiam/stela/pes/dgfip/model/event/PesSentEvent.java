package fr.sictiam.stela.pes.dgfip.model.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class PesSentEvent extends PesEvent {

    public PesSentEvent() {
    }

    public PesSentEvent(String pesId) {
        super(pesId, EventType.SENT);
    }
}
