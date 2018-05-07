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
 *         &lt;element name="IdColl" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="majauto" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "sessionId", "idColl", "majauto" })
@XmlRootElement(name = "getPESRetourRequest")
public class GetPESRetourRequest {

    @XmlElement(required = true)
    protected String sessionId;
    @XmlElement(name = "IdColl", required = true)
    protected String idColl;
    @XmlElement(required = true, type = Integer.class, defaultValue = "0", nillable = true)
    protected Integer majauto;

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
     * Obtient la valeur de la propriété idColl.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getIdColl() {
        return idColl;
    }

    /**
     * Définit la valeur de la propriété idColl.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setIdColl(String value) {
        this.idColl = value;
    }

    /**
     * Obtient la valeur de la propriété majauto.
     *
     * @return possible object is {@link Integer }
     * 
     */
    public Integer getMajauto() {
        return majauto;
    }

    /**
     * Définit la valeur de la propriété majauto.
     *
     * @param value
     *            allowed object is {@link Integer }
     * 
     */
    public void setMajauto(Integer value) {
        this.majauto = value;
    }

}
