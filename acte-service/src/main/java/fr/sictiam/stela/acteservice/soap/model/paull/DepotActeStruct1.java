//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.0-b170531.0717 
// Voir <a href="https://jaxb.java.net/">https://jaxb.java.net/</a> 
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source. 
// Généré le : 2018.05.24 à 03:28:40 PM CEST 
//


package fr.sictiam.stela.acteservice.soap.model.paull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour depotActeStruct1 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="depotActeStruct1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="dateDecision" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="numInterne" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="natureActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="matiereActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="objet" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="precedent_acte_id" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="validation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="email" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "depotActeStruct1", propOrder = {
    "dateDecision",
    "numInterne",
    "natureActe",
    "matiereActe",
    "objet",
    "precedentActeId",
    "name",
    "desc",
    "validation",
    "email"
})
public class DepotActeStruct1 {

    @XmlElement(required = true)
    protected String dateDecision;
    @XmlElement(required = true)
    protected String numInterne;
    @XmlElement(required = true)
    protected String natureActe;
    @XmlElement(required = true)
    protected String matiereActe;
    @XmlElement(required = true)
    protected String objet;
    @XmlElement(name = "precedent_acte_id", required = true)
    protected String precedentActeId;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected String validation;
    @XmlElement(required = true, nillable = true)
    protected String email;

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
     * Obtient la valeur de la propriété numInterne.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNumInterne() {
        return numInterne;
    }

    /**
     * Définit la valeur de la propriété numInterne.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNumInterne(String value) {
        this.numInterne = value;
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
     * Obtient la valeur de la propriété name.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété desc.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Définit la valeur de la propriété desc.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Obtient la valeur de la propriété validation.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValidation() {
        return validation;
    }

    /**
     * Définit la valeur de la propriété validation.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValidation(String value) {
        this.validation = value;
    }

    /**
     * Obtient la valeur de la propriété email.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getEmail() {
        return email;
    }

    /**
     * Définit la valeur de la propriété email.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEmail(String value) {
        this.email = value;
    }

}
