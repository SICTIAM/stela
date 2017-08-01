package fr.sictiam.stela.acteservice.model;

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

    public Enveloppe(String filename){
        this.setTemplateFile("/templates/EnveloppeTemplate.groovy");
        this.filename = filename;
    }

    public String getFilename(){
        return this.filename;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }
    public String getSiren(){
        return this.siren;
    }

    public void setSiren(String siren){
        this.siren = siren;
    }

     public String getDepartement(){
        return this.departement;
    }

    public void setDepartement(String departement){
        this.departement = departement;
    }

    public String getArrondissement(){
        return this.arrondissement;
    }

    public void setArrondissement(String arrondissement){
        this.arrondissement = arrondissement;
    }

     public String getNature(){
        return this.nature;
    }

    public void setNature(String nature){
        this.nature = nature;
    }

     public String getContactName(){
        return this.contactName;
    }
    
    public void setContactName(String name){
        this.contactName = name;
    }

     public String getContactPhoneNumber(){
         return this.contactPhoneNumber;
    }

    public void setContactPhoneNumber(String phoneNumber){
        this.contactPhoneNumber = phoneNumber;
    }

     public String getContactEmail(){
         return this.contactEmail;
    }

    public void setContactEmail(String contactEmail){
        this.contactEmail = contactEmail;
    }

     public String getMessageFilename(){
        return this.messageFilename;
    }

    public void setMessageFilename(String messageFilename){
        this.messageFilename = messageFilename;
    }

     public List<String> getCallbackEmails(){
        return this.callbackEmails;
    }

    public void setCallbackEmails(List<String> callbackEmails){
        this.callbackEmails = callbackEmails;
    }

    

    protected Map<String, Object> createContext(){
        Map<String, Object> result = new HashMap<String, Object>();

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