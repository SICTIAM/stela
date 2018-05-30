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
 * <p>Classe Java pour depotActeStruct complex type.
 * 
 * <p>Le fragment de schéma suivant indique le contenu attendu figurant dans cette classe.
 * 
 * <pre>
 * &lt;complexType name="depotActeStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="idActe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "depotActeStruct", propOrder = {
    "message",
    "idActe"
})
public class DepotActeStruct {

    @XmlElement(required = true)
    protected String message;
    @XmlElement(required = true)
    protected String idActe;

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
     * Obtient la valeur de la propriété idActe.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIdActe() {
        return idActe;
    }

    /**
     * Définit la valeur de la propriété idActe.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIdActe(String value) {
        this.idActe = value;
    }

}
