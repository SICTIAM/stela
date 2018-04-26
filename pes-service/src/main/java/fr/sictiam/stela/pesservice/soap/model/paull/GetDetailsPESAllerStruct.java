//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.0-b170531.0717
// Voir <a href="https://jaxb.java.net/">https://jaxb.java.net/</a>
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source.
// Généré le : 2018.04.26 à 06:13:38 PM CEST
//

package fr.sictiam.stela.pesservice.soap.model.paull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Classe Java pour getDetailsPESAllerStruct complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="getDetailsPESAllerStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="PESPJ" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="objet" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userName" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nomDocument" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateDepot" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateAR" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateAnomalie" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="motifAnomalie" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="motifPlusAnomalie" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="userNameBannette" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateDepotBannette" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="statutBannette" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="etatclasseur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="acteurCourant" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="nomClasseur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="circuitClasseur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="actionsClasseur" type="{http://www.processmaker.com}getDetailsPESAllerStruct1" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDetailsPESAllerStruct", propOrder = { "message", "pespj", "objet", "userName", "nomDocument",
        "dateDepot", "dateAR", "dateAnomalie", "motifAnomalie", "motifPlusAnomalie", "userNameBannette",
        "dateDepotBannette", "statutBannette", "etatclasseur", "acteurCourant", "nomClasseur", "circuitClasseur",
        "actionsClasseur" })
public class GetDetailsPESAllerStruct {

    @XmlElement(required = true)
    protected String message;
    @XmlElement(name = "PESPJ", required = true)
    protected String pespj;
    @XmlElement(required = true)
    protected String objet;
    @XmlElement(required = true)
    protected String userName;
    @XmlElement(required = true)
    protected String nomDocument;
    @XmlElement(required = true)
    protected String dateDepot;
    @XmlElement(required = true)
    protected String dateAR;
    @XmlElement(required = true)
    protected String dateAnomalie;
    @XmlElement(required = true)
    protected String motifAnomalie;
    @XmlElement(required = true)
    protected String motifPlusAnomalie;
    @XmlElement(required = true)
    protected String userNameBannette;
    @XmlElement(required = true)
    protected String dateDepotBannette;
    @XmlElement(required = true)
    protected String statutBannette;
    @XmlElement(required = true)
    protected String etatclasseur;
    @XmlElement(required = true)
    protected String acteurCourant;
    @XmlElement(required = true)
    protected String nomClasseur;
    @XmlElement(required = true)
    protected String circuitClasseur;
    @XmlElement(required = true)
    protected List<GetDetailsPESAllerStruct1> actionsClasseur;

    /**
     * Obtient la valeur de la propriété message.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getMessage() {
        return message;
    }

    /**
     * Définit la valeur de la propriété message.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Obtient la valeur de la propriété pespj.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getPESPJ() {
        return pespj;
    }

    /**
     * Définit la valeur de la propriété pespj.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setPESPJ(String value) {
        this.pespj = value;
    }

    /**
     * Obtient la valeur de la propriété objet.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getObjet() {
        return objet;
    }

    /**
     * Définit la valeur de la propriété objet.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setObjet(String value) {
        this.objet = value;
    }

    /**
     * Obtient la valeur de la propriété userName.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getUserName() {
        return userName;
    }

    /**
     * Définit la valeur de la propriété userName.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setUserName(String value) {
        this.userName = value;
    }

    /**
     * Obtient la valeur de la propriété nomDocument.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getNomDocument() {
        return nomDocument;
    }

    /**
     * Définit la valeur de la propriété nomDocument.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setNomDocument(String value) {
        this.nomDocument = value;
    }

    /**
     * Obtient la valeur de la propriété dateDepot.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getDateDepot() {
        return dateDepot;
    }

    /**
     * Définit la valeur de la propriété dateDepot.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDateDepot(String value) {
        this.dateDepot = value;
    }

    /**
     * Obtient la valeur de la propriété dateAR.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getDateAR() {
        return dateAR;
    }

    /**
     * Définit la valeur de la propriété dateAR.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDateAR(String value) {
        this.dateAR = value;
    }

    /**
     * Obtient la valeur de la propriété dateAnomalie.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getDateAnomalie() {
        return dateAnomalie;
    }

    /**
     * Définit la valeur de la propriété dateAnomalie.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDateAnomalie(String value) {
        this.dateAnomalie = value;
    }

    /**
     * Obtient la valeur de la propriété motifAnomalie.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getMotifAnomalie() {
        return motifAnomalie;
    }

    /**
     * Définit la valeur de la propriété motifAnomalie.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMotifAnomalie(String value) {
        this.motifAnomalie = value;
    }

    /**
     * Obtient la valeur de la propriété motifPlusAnomalie.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getMotifPlusAnomalie() {
        return motifPlusAnomalie;
    }

    /**
     * Définit la valeur de la propriété motifPlusAnomalie.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMotifPlusAnomalie(String value) {
        this.motifPlusAnomalie = value;
    }

    /**
     * Obtient la valeur de la propriété userNameBannette.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getUserNameBannette() {
        return userNameBannette;
    }

    /**
     * Définit la valeur de la propriété userNameBannette.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setUserNameBannette(String value) {
        this.userNameBannette = value;
    }

    /**
     * Obtient la valeur de la propriété dateDepotBannette.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getDateDepotBannette() {
        return dateDepotBannette;
    }

    /**
     * Définit la valeur de la propriété dateDepotBannette.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setDateDepotBannette(String value) {
        this.dateDepotBannette = value;
    }

    /**
     * Obtient la valeur de la propriété statutBannette.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getStatutBannette() {
        return statutBannette;
    }

    /**
     * Définit la valeur de la propriété statutBannette.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setStatutBannette(String value) {
        this.statutBannette = value;
    }

    /**
     * Obtient la valeur de la propriété etatclasseur.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getEtatclasseur() {
        return etatclasseur;
    }

    /**
     * Définit la valeur de la propriété etatclasseur.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setEtatclasseur(String value) {
        this.etatclasseur = value;
    }

    /**
     * Obtient la valeur de la propriété acteurCourant.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getActeurCourant() {
        return acteurCourant;
    }

    /**
     * Définit la valeur de la propriété acteurCourant.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setActeurCourant(String value) {
        this.acteurCourant = value;
    }

    /**
     * Obtient la valeur de la propriété nomClasseur.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getNomClasseur() {
        return nomClasseur;
    }

    /**
     * Définit la valeur de la propriété nomClasseur.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setNomClasseur(String value) {
        this.nomClasseur = value;
    }

    /**
     * Obtient la valeur de la propriété circuitClasseur.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getCircuitClasseur() {
        return circuitClasseur;
    }

    /**
     * Définit la valeur de la propriété circuitClasseur.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setCircuitClasseur(String value) {
        this.circuitClasseur = value;
    }

    /**
     * Gets the value of the actionsClasseur property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the actionsClasseur property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getActionsClasseur().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetDetailsPESAllerStruct1 }
     *
     *
     */
    public List<GetDetailsPESAllerStruct1> getActionsClasseur() {
        if (actionsClasseur == null) {
            actionsClasseur = new ArrayList<GetDetailsPESAllerStruct1>();
        }
        return this.actionsClasseur;
    }

}
