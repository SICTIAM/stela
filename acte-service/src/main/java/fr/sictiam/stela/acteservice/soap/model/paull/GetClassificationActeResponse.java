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
 *         &lt;element name="statusCode" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="retour" type="{http://www.processmaker.com}getClassificationActeStruct"/&gt;
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
    "statusCode",
    "retour"
})
@XmlRootElement(name = "getClassificationActeResponse")
public class GetClassificationActeResponse {

    @XmlElement(required = true)
    protected String statusCode;
    @XmlElement(required = true)
    protected GetClassificationActeStruct retour;

    /**
     * Obtient la valeur de la propriété statusCode.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStatusCode() {
        return statusCode;
    }

    /**
     * Définit la valeur de la propriété statusCode.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStatusCode(String value) {
        this.statusCode = value;
    }

    /**
     * Obtient la valeur de la propriété retour.
     * 
     * @return
     *     possible object is
     *     {@link GetClassificationActeStruct }
     *     
     */
    public GetClassificationActeStruct getRetour() {
        return retour;
    }

    /**
     * Définit la valeur de la propriété retour.
     * 
     * @param value
     *     allowed object is
     *     {@link GetClassificationActeStruct }
     *     
     */
    public void setRetour(GetClassificationActeStruct value) {
        this.retour = value;
    }

}
