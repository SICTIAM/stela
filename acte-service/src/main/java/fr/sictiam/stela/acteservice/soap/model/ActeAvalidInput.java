package fr.sictiam.stela.acteservice.soap.model;

//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.2.11
// Voir <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source.
// Généré le : 2018.03.09 à 04:43:27 PM CET
//

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour Acte_avalid_Input complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="Acte_avalid_Input"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="form_id" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Acte_avalid_Input", propOrder = { "formId" })
@XmlRootElement(name = "getgetStatusActe_avalid")
public class ActeAvalidInput {

    @XmlElement(name = "form_id", required = true)
    protected String formId;

    /**
     * Obtient la valeur de la propriété formId.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFormId() {
        return formId;
    }

    /**
     * Définit la valeur de la propriété formId.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFormId(String value) {
        this.formId = value;
    }

}
