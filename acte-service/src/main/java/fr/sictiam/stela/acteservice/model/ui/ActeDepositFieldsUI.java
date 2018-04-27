package fr.sictiam.stela.acteservice.model.ui;

public class ActeDepositFieldsUI {

    private boolean isPublicField;
    private boolean isPublicWebsiteField;

    public ActeDepositFieldsUI(boolean isPublicField, boolean isPublicWebsiteField) {
        this.isPublicField = isPublicField;
        this.isPublicWebsiteField = isPublicWebsiteField;
    }

    public boolean isPublicField() {
        return isPublicField;
    }

    public boolean isPublicWebsiteField() {
        return isPublicWebsiteField;
    }
}
