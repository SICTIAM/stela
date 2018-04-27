//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.11
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source.
// Généré le : 2018.03.09 à 04:43:27 PM CET
//

package fr.sictiam.stela.acteservice.soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour GetAnomaliesEnveloppe_Input complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="GetAnomaliesEnveloppe_Input"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="enveloppe_id" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetAnomaliesEnveloppe_Input", propOrder = { "enveloppeId" })
public class GetAnomaliesEnveloppeInput {

    @XmlElement(name = "enveloppe_id", required = true)
    protected String enveloppeId;

    /**
     * Obtient la valeur de la propriété enveloppeId.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEnveloppeId() {
        return enveloppeId;
    }

    /**
     * Définit la valeur de la propriété enveloppeId.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEnveloppeId(String value) {
        this.enveloppeId = value;
    }

}
