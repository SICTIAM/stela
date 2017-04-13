package fr.sictiam.stela.pescommand.event;

public class PesCreatedEvent {

    private String id;

    public PesCreatedEvent() {
    }

    public PesCreatedEvent(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
