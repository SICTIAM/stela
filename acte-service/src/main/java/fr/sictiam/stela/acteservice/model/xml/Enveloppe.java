package fr.sictiam.stela.acteservice.model.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the enveloppe's data.
 * The enveloppe is mandatory in all transaction with minister.
 */
public class Enveloppe extends XmlBase {

    private String filename;
    private String siren;
    private String departement;
    private String arrondissement;
    private String nature;
    private String contactName;
    private String contactPhoneNumber;
    private String contactEmail;

    private List<String> callbackEmails;

    // TODO : could be a list as an enveloppe can reference several messages.
    private String messageFilename;

    public Enveloppe(String filename, String siren, String departement, String arrondissement, String nature,
                     String contactName, String contactPhoneNumber, String contactEmail, List<String> callbackEmails,
                     String messageFilename) {
        this.setTemplateFile("/templates/EnveloppeTemplate.groovy");
        this.filename = filename;
        this.siren = siren;
        this.departement = departement;
        this.arrondissement = arrondissement;
        this.nature = nature;
        this.contactName = contactName;
        this.contactPhoneNumber = contactPhoneNumber;
        this.contactEmail = contactEmail;
        this.callbackEmails = callbackEmails;
        this.messageFilename = messageFilename;
    }

    public String getFilename() {
        return this.filename;
    }

    public String getSiren() {
        return this.siren;
    }

    public String getDepartement() {
        return this.departement;
    }

    public String getArrondissement() {
        return this.arrondissement;
    }

    public String getNature() {
        return this.nature;
    }

    public String getContactName() {
        return this.contactName;
    }

    public String getContactPhoneNumber() {
        return this.contactPhoneNumber;
    }

    public String getContactEmail() {
        return this.contactEmail;
    }

    public String getMessageFilename() {
        return this.messageFilename;
    }

    public List<String> getCallbackEmails() {
        return this.callbackEmails;
    }

    protected Map<String, Object> createContext() {
        Map<String, Object> result = new HashMap<>();

        result.put("departement", this.departement);
        result.put("arrondissement", this.arrondissement);
        result.put("siren", this.siren);
        result.put("nature", this.nature);
        result.put("contactName", this.contactName);
        result.put("contactPhoneNumber", this.contactPhoneNumber);
        result.put("contactEmail", this.contactEmail);
        result.put("messageFilename", this.messageFilename);
        result.put("callbackEmails", this.callbackEmails);

        return result;
    }
}