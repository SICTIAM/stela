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
 * <p>Classe Java pour getClassificationActeStruct complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="getClassificationActeStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="codeMatiere" type="{http://www.processmaker.com}getClassificationActeStruct1" maxOccurs="unbounded"/&gt;
 *         &lt;element name="natureActes" type="{http://www.processmaker.com}getClassificationActeStruct1" maxOccurs="unbounded"/&gt;
 *         &lt;element name="collectiviteDateClassification" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getClassificationActeStruct", propOrder = {
    "message",
    "codeMatiere",
    "natureActes",
    "collectiviteDateClassification"
})
public class GetClassificationActeStruct {

    @XmlElement(required = true)
    protected String message;
    @XmlElement(required = true)
    protected List<GetClassificationActeStruct1> codeMatiere;
    @XmlElement(required = true)
    protected List<GetClassificationActeStruct1> natureActes;
    @XmlElement(required = true)
    protected String collectiviteDateClassification;

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
     * Gets the value of the codeMatiere property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the codeMatiere property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getCodeMatiere().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetClassificationActeStruct1 }
     * 
     * 
     */
    public List<GetClassificationActeStruct1> getCodeMatiere() {
        if (codeMatiere == null) {
            codeMatiere = new ArrayList<GetClassificationActeStruct1>();
        }
        return this.codeMatiere;
    }

    /**
     * Gets the value of the natureActes property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the natureActes property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getNatureActes().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetClassificationActeStruct1 }
     * 
     * 
     */
    public List<GetClassificationActeStruct1> getNatureActes() {
        if (natureActes == null) {
            natureActes = new ArrayList<GetClassificationActeStruct1>();
        }
        return this.natureActes;
    }

    /**
     * Obtient la valeur de la propriété collectiviteDateClassification.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectiviteDateClassification() {
        return collectiviteDateClassification;
    }

    /**
     * Définit la valeur de la propriété collectiviteDateClassification.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectiviteDateClassification(String value) {
        this.collectiviteDateClassification = value;
    }

}
