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
 * Classe Java pour GetDocumentComplementaire_Output complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="GetDocumentComplementaire_Output"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jsonGetDocumentComplementaire" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetDocumentComplementaire_Output", propOrder = { "jsonGetDocumentComplementaire" })
@XmlRootElement(name = "setGetDocumentComplementaire")
public class GetDocumentComplementaireOutput {

    @XmlElement(required = true)
    protected String jsonGetDocumentComplementaire;

    /**
     * Obtient la valeur de la propriété jsonGetDocumentComplementaire.
     *
     * @return possible object is {@link String }
     *
     */
    public String getJsonGetDocumentComplementaire() {
        return jsonGetDocumentComplementaire;
    }

    /**
     * Définit la valeur de la propriété jsonGetDocumentComplementaire.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setJsonGetDocumentComplementaire(String value) {
        this.jsonGetDocumentComplementaire = value;
    }

}
