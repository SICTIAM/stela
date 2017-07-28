package fr.sictiam.stela.admin.model;

import org.apache.commons.lang.RandomStringUtils;

import javax.persistence.Embeddable;

@Embeddable
public class OzwilloInstanceInfo {

    private String instanceId;
    private String clientId;
    private String clientSecret;
    private String instanceRegistrationUri;
    private String destructionSecret;
    private String statusChangedSecret;
    private String creatorId;
    private String creatorName;
    private String kernelId;
    private String dcId;
    private boolean notifiedToKernel = false;

    public OzwilloInstanceInfo() {
    }

    public OzwilloInstanceInfo(String instanceId, String clientId, String clientSecret, String instanceRegistrationUri,
                               String creatorId, String creatorName, String kernelId, String dcId) {
        this.instanceId = instanceId;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.instanceRegistrationUri = instanceRegistrationUri;
        this.destructionSecret = RandomStringUtils.randomAlphanumeric(48);
        this.statusChangedSecret = RandomStringUtils.randomAlphanumeric(48);
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.kernelId = kernelId;
        this.dcId = dcId;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getInstanceRegistrationUri() {
        return instanceRegistrationUri;
    }

    public String getDestructionSecret() {
        return destructionSecret;
    }

    public String getStatusChangedSecret() {
        return statusChangedSecret;
    }

    public String getCreatorId() {
        return creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public String getKernelId() {
        return kernelId;
    }

    public String getDcId() {
        return dcId;
    }

    public boolean isNotifiedToKernel() {
        return notifiedToKernel;
    }

    public void setNotifiedToKernel(boolean notifiedToKernel) {
        this.notifiedToKernel = notifiedToKernel;
    }

    @Override
    public String toString() {
        return "OzwilloInstanceInfo{" +
                "instanceId='" + instanceId + '\'' +
                ", clientId='" + clientId + '\'' +
                ", clientSecret='" + clientSecret + '\'' +
                ", instanceRegistrationUri='" + instanceRegistrationUri + '\'' +
                ", creatorId='" + creatorId + '\'' +
                ", creatorName='" + creatorName + '\'' +
                ", kernelId='" + kernelId + '\'' +
                ", dcId='" + dcId + '\'' +
                ", notifiedToKernel=" + notifiedToKernel +
                '}';
    }
}
