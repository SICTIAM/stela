package fr.sictiam.stela.pes.model.event;

public class PesSentEvent extends PesEvent {

    public PesSentEvent() {
    }

    public PesSentEvent(String pesId) {
        super(pesId, EventType.SENT);
    }
}
