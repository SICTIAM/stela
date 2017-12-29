package fr.sictiam.stela.apigateway.model;

public class LocalAuthorityInstance {

    private String clientId;
    private String clientSecret;

    protected LocalAuthorityInstance() {}

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }
}
