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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Classe Java pour anonymous complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="sessionId" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="infosPESAller" type="{http://www.processmaker.com}depotPESAllerStruct1" maxOccurs="unbounded"/&gt;
 *         &lt;element name="fichier" type="{http://www.processmaker.com}depotPESAllerStruct3" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "sessionId", "infosPESAller", "fichier" })
@XmlRootElement(name = "depotPESAllerRequest")
public class DepotPESAllerRequest {

    @XmlElement(required = true)
    protected String sessionId;
    @XmlElement(required = true)
    protected List<DepotPESAllerStruct1> infosPESAller;
    @XmlElement(required = true)
    protected List<DepotPESAllerStruct3> fichier;

    /**
     * Obtient la valeur de la propriété sessionId.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getSessionId() {
        return sessionId;
    }

    /**
     * Définit la valeur de la propriété sessionId.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setSessionId(String value) {
        this.sessionId = value;
    }

    /**
     * Gets the value of the infosPESAller property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the infosPESAller property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getInfosPESAller().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DepotPESAllerStruct1 }
     *
     *
     */
    public List<DepotPESAllerStruct1> getInfosPESAller() {
        if (infosPESAller == null) {
            infosPESAller = new ArrayList<DepotPESAllerStruct1>();
        }
        return this.infosPESAller;
    }

    /**
     * Gets the value of the fichier property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the fichier property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getFichier().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link DepotPESAllerStruct3 }
     *
     *
     */
    public List<DepotPESAllerStruct3> getFichier() {
        if (fichier == null) {
            fichier = new ArrayList<DepotPESAllerStruct3>();
        }
        return this.fichier;
    }

}
