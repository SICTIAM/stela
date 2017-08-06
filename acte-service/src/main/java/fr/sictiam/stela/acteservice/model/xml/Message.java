package fr.sictiam.stela.acteservice.model.xml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Message file is used to describe and reference the files joined with a transmission.
 */
public class Message extends XmlBase {

    private String filename;
    private String natureCode;
    private String decisionDate;
    private String number;

    private String codeMatiere1;
    private String codeMatiere2;
    private String codeMatiere3;
    private String codeMatiere4;
    private String codeMatiere5;

    private String acteTitle;
    private String acteFilename;

    private List<String> annexesFilenames;

    public Message(String filename){
        this.filename = filename;
        this.setTemplateFile("/templates/MessageTemplate.groovy");
    }

    public String getFilename(){
        return this.filename;
    }

    public void setFilename(String filename){
        this.filename = filename;
    }

    public String getNatureCode(){
        return this.natureCode;
    }

    public void setNatureCode(String natureCode){
        this.natureCode = natureCode;
    }

    public String getDecisionDate(){
        return this.decisionDate;
    }

    public void setDecisionDate(String decisionDate){
        this.decisionDate = decisionDate;
    }

    public String getNumber(){
        return this.number;
    }

    public void setNumber(String number){
        this.number = number;
    }

    public String getCodeMatiere1(){
        return this.codeMatiere1;
    }

    public void setCodeMatiere1(String codeMatiere){
        this.codeMatiere1 = codeMatiere;
    }

    public String getCodeMatiere2(){
        return this.codeMatiere2;
    }

    public void setCodeMatiere2(String codeMatiere){
        this.codeMatiere2 = codeMatiere;
    }
    public String getCodeMatiere3(){
        return this.codeMatiere3;
    }

    public void setCodeMatiere3(String codeMatiere){
        this.codeMatiere3 = codeMatiere;
    }
    public String getCodeMatiere4(){
        return this.codeMatiere4;
    }

    public void setCodeMatiere4(String codeMatiere){
        this.codeMatiere4 = codeMatiere;
    }
    public String getCodeMatiere5(){
        return this.codeMatiere5;
    }

    public void setCodeMatiere5(String codeMatiere){
        this.codeMatiere5 = codeMatiere;
    }

    public String getActeTitle(){
        return this.acteTitle;
    }

    public void setActeTitle(String subject){
        this.acteTitle = subject;
    }

    public String getActeFilename(){
        return this.acteFilename;
    }

    public void setActeFilename(String acteFilename){
        this.acteFilename = acteFilename;
    }

    public List<String> getAnnexesFilenames(){
        return this.annexesFilenames;
    }

    public void setAnnexesFilenames(List<String> annexesFilenames){
        this.annexesFilenames = annexesFilenames;
    }

    protected Map<String, Object> createContext(){
        Map<String, Object> result = new HashMap<>();

        result.put("natureCode", this.natureCode);
        result.put("decisionDate", this.decisionDate);
        result.put("number", this.number);
        result.put("acteTitle", this.acteTitle);
        result.put("codeMatiere1", this.codeMatiere1);
        result.put("codeMatiere2", this.codeMatiere2);
        result.put("codeMatiere3", this.codeMatiere3);
        result.put("codeMatiere4", this.codeMatiere4);
        result.put("codeMatiere5", this.codeMatiere5);
        result.put("acteFilename", this.acteFilename);
        result.put("annexesFilenames", this.annexesFilenames);

        return result;
    }
}