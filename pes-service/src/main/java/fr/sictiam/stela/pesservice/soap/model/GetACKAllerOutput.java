//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.11
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source.
// Généré le : 2018.03.21 à 05:01:04 PM CET
//

package fr.sictiam.stela.pesservice.soap.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour getACKAller_Output complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="getACKAller_Output"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jsonGetACKAller" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getACKAller_Output", propOrder = { "jsonGetACKAller" })
@XmlRootElement(name = "setGetACKAller")
public class GetACKAllerOutput {

    @XmlElement(required = true)
    protected String jsonGetACKAller;

    /**
     * Obtient la valeur de la propriété jsonGetACKAller.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getJsonGetACKAller() {
        return jsonGetACKAller;
    }

    /**
     * Définit la valeur de la propriété jsonGetACKAller.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setJsonGetACKAller(String value) {
        this.jsonGetACKAller = value;
    }

}
