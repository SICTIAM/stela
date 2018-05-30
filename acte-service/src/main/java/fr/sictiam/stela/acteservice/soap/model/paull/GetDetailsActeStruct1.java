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
 * <p>Classe Java pour getDetailsActeStruct1 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="getDetailsActeStruct1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="nomActeur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="dateAction" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="libelleAction" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getDetailsActeStruct1", propOrder = {
    "nomActeur",
    "dateAction",
    "libelleAction"
})
public class GetDetailsActeStruct1 {

    @XmlElement(required = true)
    protected String nomActeur;
    @XmlElement(required = true)
    protected String dateAction;
    @XmlElement(required = true)
    protected String libelleAction;

    /**
     * Obtient la valeur de la propriété nomActeur.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNomActeur() {
        return nomActeur;
    }

    /**
     * Définit la valeur de la propriété nomActeur.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNomActeur(String value) {
        this.nomActeur = value;
    }

    /**
     * Obtient la valeur de la propriété dateAction.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDateAction() {
        return dateAction;
    }

    /**
     * Définit la valeur de la propriété dateAction.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateAction(String value) {
        this.dateAction = value;
    }

    /**
     * Obtient la valeur de la propriété libelleAction.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLibelleAction() {
        return libelleAction;
    }

    /**
     * Définit la valeur de la propriété libelleAction.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLibelleAction(String value) {
        this.libelleAction = value;
    }

}
