//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.0-b170531.0717 
// Voir <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2018.05.24 à 03:28:40 PM CEST 
//


package fr.sictiam.stela.acteservice.soap.model.paull;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour getDetailsActeStruct complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="getDetailsActeStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="numActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="miatID" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="precedentActeId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="objet" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userNameDeposantBannette" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="natureActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="matiereActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nomDocument" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="annexesList" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="statut" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateDecision" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateDepotBannette" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateDepotActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateAR" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateAnnul" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateARAnnul" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="anomalies" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="courrierSimple" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="reponseCourrier_simple" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="lettreObservations" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="reponseLettreObservations" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="refusLettreObservations" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="demandePC" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="reponseDemandePC" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="refusDemandePC" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="defer" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="etatClasseur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="acteurCourant" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nomClasseur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="circuitClasseur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="actionsClasseur" type="{http://www.processmaker.com}getDetailsActeStruct1" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDetailsActeStruct", propOrder = {
    "message",
    "numActe",
    "miatID",
    "precedentActeId",
    "objet",
    "userName",
    "userNameDeposantBannette",
    "natureActe",
    "matiereActe",
    "nomDocument",
    "annexesList",
    "statut",
    "dateDecision",
    "dateDepotBannette",
    "dateDepotActe",
    "dateAR",
    "dateAnnul",
    "dateARAnnul",
    "anomalies",
    "courrierSimple",
    "reponseCourrierSimple",
    "lettreObservations",
    "reponseLettreObservations",
    "refusLettreObservations",
    "demandePC",
    "reponseDemandePC",
    "refusDemandePC",
    "defer",
    "etatClasseur",
    "acteurCourant",
    "nomClasseur",
    "circuitClasseur",
    "actionsClasseur"
})
public class GetDetailsActeStruct {

    @XmlElement(required = true)
    protected String message;
    @XmlElement(required = true)
    protected String numActe;
    @XmlElement(required = true)
    protected String miatID;
    @XmlElement(required = true)
    protected String precedentActeId;
    @XmlElement(required = true)
    protected String objet;
    @XmlElement(required = true)
    protected String userName;
    @XmlElement(required = true)
    protected String userNameDeposantBannette;
    @XmlElement(required = true)
    protected String natureActe;
    @XmlElement(required = true)
    protected String matiereActe;
    @XmlElement(required = true)
    protected String nomDocument;
    @XmlElement(required = true)
    protected String annexesList;
    @XmlElement(required = true)
    protected String statut;
    @XmlElement(required = true)
    protected String dateDecision;
    @XmlElement(required = true)
    protected String dateDepotBannette;
    @XmlElement(required = true)
    protected String dateDepotActe;
    @XmlElement(required = true)
    protected String dateAR;
    @XmlElement(required = true)
    protected String dateAnnul;
    @XmlElement(required = true)
    protected String dateARAnnul;
    @XmlElement(required = true)
    protected String anomalies;
    @XmlElement(required = true)
    protected String courrierSimple;
    @XmlElement(name = "reponseCourrier_simple", required = true)
    protected String reponseCourrierSimple;
    @XmlElement(required = true)
    protected String lettreObservations;
    @XmlElement(required = true)
    protected String reponseLettreObservations;
    @XmlElement(required = true)
    protected String refusLettreObservations;
    @XmlElement(required = true)
    protected String demandePC;
    @XmlElement(required = true)
    protected String reponseDemandePC;
    @XmlElement(required = true)
    protected String refusDemandePC;
    @XmlElement(required = true)
    protected String defer;
    @XmlElement(required = true)
    protected String etatClasseur;
    @XmlElement(required = true)
    protected String acteurCourant;
    @XmlElement(required = true)
    protected String nomClasseur;
    @XmlElement(required = true)
    protected String circuitClasseur;
    @XmlElement(required = true)
    protected List<GetDetailsActeStruct1> actionsClasseur;

    /**
     * Obtient la valeur de la propriété message.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMessage() {
        return message;
    }

    /**
     * Définit la valeur de la propriété message.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Obtient la valeur de la propriété numActe.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumActe() {
        return numActe;
    }

    /**
     * Définit la valeur de la propriété numActe.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumActe(String value) {
        this.numActe = value;
    }

    /**
     * Obtient la valeur de la propriété miatID.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMiatID() {
        return miatID;
    }

    /**
     * Définit la valeur de la propriété miatID.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMiatID(String value) {
        this.miatID = value;
    }

    /**
     * Obtient la valeur de la propriété precedentActeId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPrecedentActeId() {
        return precedentActeId;
    }

    /**
     * Définit la valeur de la propriété precedentActeId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPrecedentActeId(String value) {
        this.precedentActeId = value;
    }

    /**
     * Obtient la valeur de la propriété objet.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getObjet() {
        return objet;
    }

    /**
     * Définit la valeur de la propriété objet.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setObjet(String value) {
        this.objet = value;
    }

    /**
     * Obtient la valeur de la propriété userName.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Définit la valeur de la propriété userName.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Obtient la valeur de la propriété userNameDeposantBannette.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getUserNameDeposantBannette() {
        return userNameDeposantBannette;
    }

    /**
     * Définit la valeur de la propriété userNameDeposantBannette.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setUserNameDeposantBannette(String value) {
        this.userNameDeposantBannette = value;
    }

    /**
     * Obtient la valeur de la propriété natureActe.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNatureActe() {
        return natureActe;
    }

    /**
     * Définit la valeur de la propriété natureActe.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNatureActe(String value) {
        this.natureActe = value;
    }

    /**
     * Obtient la valeur de la propriété matiereActe.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMatiereActe() {
        return matiereActe;
    }

    /**
     * Définit la valeur de la propriété matiereActe.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setMatiereActe(String value) {
        this.matiereActe = value;
    }

    /**
     * Obtient la valeur de la propriété nomDocument.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomDocument() {
        return nomDocument;
    }

    /**
     * Définit la valeur de la propriété nomDocument.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomDocument(String value) {
        this.nomDocument = value;
    }

    /**
     * Obtient la valeur de la propriété annexesList.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnnexesList() {
        return annexesList;
    }

    /**
     * Définit la valeur de la propriété annexesList.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnnexesList(String value) {
        this.annexesList = value;
    }

    /**
     * Obtient la valeur de la propriété statut.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatut() {
        return statut;
    }

    /**
     * Définit la valeur de la propriété statut.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatut(String value) {
        this.statut = value;
    }

    /**
     * Obtient la valeur de la propriété dateDecision.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateDecision() {
        return dateDecision;
    }

    /**
     * Définit la valeur de la propriété dateDecision.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateDecision(String value) {
        this.dateDecision = value;
    }

    /**
     * Obtient la valeur de la propriété dateDepotBannette.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateDepotBannette() {
        return dateDepotBannette;
    }

    /**
     * Définit la valeur de la propriété dateDepotBannette.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateDepotBannette(String value) {
        this.dateDepotBannette = value;
    }

    /**
     * Obtient la valeur de la propriété dateDepotActe.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateDepotActe() {
        return dateDepotActe;
    }

    /**
     * Définit la valeur de la propriété dateDepotActe.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateDepotActe(String value) {
        this.dateDepotActe = value;
    }

    /**
     * Obtient la valeur de la propriété dateAR.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateAR() {
        return dateAR;
    }

    /**
     * Définit la valeur de la propriété dateAR.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateAR(String value) {
        this.dateAR = value;
    }

    /**
     * Obtient la valeur de la propriété dateAnnul.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateAnnul() {
        return dateAnnul;
    }

    /**
     * Définit la valeur de la propriété dateAnnul.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateAnnul(String value) {
        this.dateAnnul = value;
    }

    /**
     * Obtient la valeur de la propriété dateARAnnul.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateARAnnul() {
        return dateARAnnul;
    }

    /**
     * Définit la valeur de la propriété dateARAnnul.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateARAnnul(String value) {
        this.dateARAnnul = value;
    }

    /**
     * Obtient la valeur de la propriété anomalies.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAnomalies() {
        return anomalies;
    }

    /**
     * Définit la valeur de la propriété anomalies.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAnomalies(String value) {
        this.anomalies = value;
    }

    /**
     * Obtient la valeur de la propriété courrierSimple.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCourrierSimple() {
        return courrierSimple;
    }

    /**
     * Définit la valeur de la propriété courrierSimple.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCourrierSimple(String value) {
        this.courrierSimple = value;
    }

    /**
     * Obtient la valeur de la propriété reponseCourrierSimple.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReponseCourrierSimple() {
        return reponseCourrierSimple;
    }

    /**
     * Définit la valeur de la propriété reponseCourrierSimple.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReponseCourrierSimple(String value) {
        this.reponseCourrierSimple = value;
    }

    /**
     * Obtient la valeur de la propriété lettreObservations.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLettreObservations() {
        return lettreObservations;
    }

    /**
     * Définit la valeur de la propriété lettreObservations.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLettreObservations(String value) {
        this.lettreObservations = value;
    }

    /**
     * Obtient la valeur de la propriété reponseLettreObservations.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReponseLettreObservations() {
        return reponseLettreObservations;
    }

    /**
     * Définit la valeur de la propriété reponseLettreObservations.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReponseLettreObservations(String value) {
        this.reponseLettreObservations = value;
    }

    /**
     * Obtient la valeur de la propriété refusLettreObservations.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefusLettreObservations() {
        return refusLettreObservations;
    }

    /**
     * Définit la valeur de la propriété refusLettreObservations.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefusLettreObservations(String value) {
        this.refusLettreObservations = value;
    }

    /**
     * Obtient la valeur de la propriété demandePC.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDemandePC() {
        return demandePC;
    }

    /**
     * Définit la valeur de la propriété demandePC.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDemandePC(String value) {
        this.demandePC = value;
    }

    /**
     * Obtient la valeur de la propriété reponseDemandePC.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getReponseDemandePC() {
        return reponseDemandePC;
    }

    /**
     * Définit la valeur de la propriété reponseDemandePC.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setReponseDemandePC(String value) {
        this.reponseDemandePC = value;
    }

    /**
     * Obtient la valeur de la propriété refusDemandePC.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRefusDemandePC() {
        return refusDemandePC;
    }

    /**
     * Définit la valeur de la propriété refusDemandePC.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRefusDemandePC(String value) {
        this.refusDemandePC = value;
    }

    /**
     * Obtient la valeur de la propriété defer.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDefer() {
        return defer;
    }

    /**
     * Définit la valeur de la propriété defer.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefer(String value) {
        this.defer = value;
    }

    /**
     * Obtient la valeur de la propriété etatClasseur.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEtatClasseur() {
        return etatClasseur;
    }

    /**
     * Définit la valeur de la propriété etatClasseur.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEtatClasseur(String value) {
        this.etatClasseur = value;
    }

    /**
     * Obtient la valeur de la propriété acteurCourant.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getActeurCourant() {
        return acteurCourant;
    }

    /**
     * Définit la valeur de la propriété acteurCourant.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setActeurCourant(String value) {
        this.acteurCourant = value;
    }

    /**
     * Obtient la valeur de la propriété nomClasseur.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomClasseur() {
        return nomClasseur;
    }

    /**
     * Définit la valeur de la propriété nomClasseur.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomClasseur(String value) {
        this.nomClasseur = value;
    }

    /**
     * Obtient la valeur de la propriété circuitClasseur.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCircuitClasseur() {
        return circuitClasseur;
    }

    /**
     * Définit la valeur de la propriété circuitClasseur.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCircuitClasseur(String value) {
        this.circuitClasseur = value;
    }

    /**
     * Gets the value of the actionsClasseur property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the actionsClasseur property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getActionsClasseur().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetDetailsActeStruct1 }
     * 
     * 
     */
    public List<GetDetailsActeStruct1> getActionsClasseur() {
        if (actionsClasseur == null) {
            actionsClasseur = new ArrayList<GetDetailsActeStruct1>();
        }
        return this.actionsClasseur;
    }

}
