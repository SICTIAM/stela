package fr.sictiam.stela.admin.model;

import java.util.List;

public class UserGatewayRequest {

    private List<String> emails;

    private OzwilloInstanceInfo ozwilloInstanceInfo;

    public UserGatewayRequest() {

    }

    public UserGatewayRequest(List<String> emails, OzwilloInstanceInfo ozwilloInstanceInfo) {
        this.emails = emails;
        this.ozwilloInstanceInfo = ozwilloInstanceInfo;
    }

    public List<String> getEmails() {
        return emails;
    }

    public void setEmails(List<String> emails) {
        this.emails = emails;
    }

    public OzwilloInstanceInfo getOzwilloInstanceInfo() {
        return ozwilloInstanceInfo;
    }

    public void setOzwilloInstanceInfo(OzwilloInstanceInfo ozwilloInstanceInfo) {
        this.ozwilloInstanceInfo = ozwilloInstanceInfo;
    }

}
