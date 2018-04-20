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
 * Classe Java pour AutoCompletion_Input complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="AutoCompletion_Input"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="numero" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="uid" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="groupeid" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AutoCompletion_Input", propOrder = { "numero", "uid", "groupeid" })
public class AutoCompletionInput {

    @XmlElement(required = true)
    protected String numero;
    @XmlElement(required = true)
    protected String uid;
    @XmlElement(required = true)
    protected String groupeid;

    /**
     * Obtient la valeur de la propriété numero.
     *
     * @return possible object is {@link String }
     *
     */
    public String getNumero() {
        return numero;
    }

    /**
     * Définit la valeur de la propriété numero.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setNumero(String value) {
        this.numero = value;
    }

    /**
     * Obtient la valeur de la propriété uid.
     *
     * @return possible object is {@link String }
     *
     */
    public String getUid() {
        return uid;
    }

    /**
     * Définit la valeur de la propriété uid.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setUid(String value) {
        this.uid = value;
    }

    /**
     * Obtient la valeur de la propriété groupeid.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGroupeid() {
        return groupeid;
    }

    /**
     * Définit la valeur de la propriété groupeid.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setGroupeid(String value) {
        this.groupeid = value;
    }

}
