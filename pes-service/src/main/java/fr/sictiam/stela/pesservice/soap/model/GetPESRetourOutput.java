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
 * Classe Java pour getPESRetour_Output complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="getPESRetour_Output"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jsonGetPESRetour" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPESRetour_Output", propOrder = { "jsonGetPESRetour" })
@XmlRootElement(name = "setGetPESRetour")
public class GetPESRetourOutput {

    @XmlElement(required = true)
    protected String jsonGetPESRetour;

    /**
     * Obtient la valeur de la propriété jsonGetPESRetour.
     *
     * @return possible object is {@link String }
     *
     */
    public String getJsonGetPESRetour() {
        return jsonGetPESRetour;
    }

    /**
     * Définit la valeur de la propriété jsonGetPESRetour.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setJsonGetPESRetour(String value) {
        this.jsonGetPESRetour = value;
    }

}
