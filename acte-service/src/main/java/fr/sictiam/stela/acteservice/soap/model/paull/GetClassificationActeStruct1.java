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
 * <p>Classe Java pour getClassificationActeStruct1 complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="getClassificationActeStruct1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="cle" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="valeur" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getClassificationActeStruct1", propOrder = {
    "cle",
    "valeur"
})
public class GetClassificationActeStruct1 {

    @XmlElement(required = true)
    protected String cle;
    @XmlElement(required = true)
    protected String valeur;

    /**
     * Obtient la valeur de la propriété cle.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCle() {
        return cle;
    }

    /**
     * Définit la valeur de la propriété cle.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCle(String value) {
        this.cle = value;
    }

    /**
     * Obtient la valeur de la propriété valeur.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValeur() {
        return valeur;
    }

    /**
     * Définit la valeur de la propriété valeur.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValeur(String value) {
        this.valeur = value;
    }

}
