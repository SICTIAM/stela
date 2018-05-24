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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Classe Java pour anonymous complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="infosActe" type="{http://www.processmaker.com}depotActeStruct1" maxOccurs="unbounded"/&gt;
 *         &lt;element name="fichiers" type="{http://www.processmaker.com}depotActeStruct2" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "sessionId",
    "infosActe",
    "fichiers"
})
@XmlRootElement(name = "depotActeRequest")
public class DepotActeRequest {

    @XmlElement(required = true)
    protected String sessionId;
    @XmlElement(required = true)
    protected List<DepotActeStruct1> infosActe;
    @XmlElement(required = true)
    protected List<DepotActeStruct2> fichiers;

    /**
     * Obtient la valeur de la propriété sessionId.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Définit la valeur de la propriété sessionId.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the infosActe property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the infosActe property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getInfosActe().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DepotActeStruct1 }
     * 
     * 
     */
    public List<DepotActeStruct1> getInfosActe() {
        if (infosActe == null) {
            infosActe = new ArrayList<DepotActeStruct1>();
        }
        return this.infosActe;
    }

    /**
     * Gets the value of the fichiers property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the fichiers property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getFichiers().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DepotActeStruct2 }
     * 
     * 
     */
    public List<DepotActeStruct2> getFichiers() {
        if (fichiers == null) {
            fichiers = new ArrayList<DepotActeStruct2>();
        }
        return this.fichiers;
    }

}
