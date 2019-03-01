package fr.sictiam.stela.convocationservice.service.util;

import fr.sictiam.stela.convocationservice.model.Convocation;

public interface DocumentGenerator {

    byte[] generatePresenceList(Convocation convocation);
}