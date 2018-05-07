package fr.sictiam.stela.admin.model;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class PaullConnection {

    @Id
    private String sessionId;

    private String token;

    public PaullConnection() {
    }

    public String getSessionId() {
        return sessionId;
    }

    public PaullConnection(String sessionId, String token) {
        super();
        this.sessionId = sessionId;
        this.token = token;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
