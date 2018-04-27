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
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour Acte_avalid_Output complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="Acte_avalid_Output"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jsongetStatusActe_avalid" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Acte_avalid_Output", propOrder = { "jsongetStatusActeAvalid" })
@XmlRootElement(name = "setgetStatusActe_avalid")
public class ActeAvalidOutput {

    @XmlElement(name = "jsongetStatusActe_avalid", required = true)
    protected String jsongetStatusActeAvalid;

    /**
     * Obtient la valeur de la propriété jsongetStatusActeAvalid.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getJsongetStatusActeAvalid() {
        return jsongetStatusActeAvalid;
    }

    /**
     * Définit la valeur de la propriété jsongetStatusActeAvalid.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setJsongetStatusActeAvalid(String value) {
        this.jsongetStatusActeAvalid = value;
    }

}
