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
 * Classe Java pour GetDocumentComplementaire_Input complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="GetDocumentComplementaire_Input"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="form_id_doc" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDocumentComplementaire_Input", propOrder = { "formIdDoc" })
@XmlRootElement(name = "getGetDocumentComplementaire")
public class GetDocumentComplementaireInput {

    @XmlElement(name = "form_id_doc", required = true)
    protected String formIdDoc;

    /**
     * Obtient la valeur de la propriété formIdDoc.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFormIdDoc() {
        return formIdDoc;
    }

    /**
     * Définit la valeur de la propriété formIdDoc.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFormIdDoc(String value) {
        this.formIdDoc = value;
    }

}
