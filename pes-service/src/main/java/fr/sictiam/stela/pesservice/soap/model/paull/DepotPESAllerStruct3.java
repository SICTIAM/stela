//
// Ce fichier a été généré par l'implémentation de référence JavaTM Architecture for XML Binding (JAXB), v2.3.0-b170531.0717
// Voir <a href="https://jaxb.java.net/">https://jaxb.java.net/</a>
// Toute modification apportée à ce fichier sera perdue lors de la recompilation du schéma source.
// Généré le : 2018.04.26 à 06:13:38 PM CEST
//

package fr.sictiam.stela.pesservice.soap.model.paull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour depotPESAllerStruct3 complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="depotPESAllerStruct3"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="filename" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="base64" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "depotPESAllerStruct3", propOrder = { "filename", "base64" })
public class DepotPESAllerStruct3 {

    @XmlElement(required = true)
    protected String filename;
    @XmlElement(required = true)
    protected String base64;

    /**
     * Obtient la valeur de la propriété filename.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getFilename() {
        return filename;
    }

    /**
     * Définit la valeur de la propriété filename.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setFilename(String value) {
        this.filename = value;
    }

    /**
     * Obtient la valeur de la propriété base64.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getBase64() {
        return base64;
    }

    /**
     * Définit la valeur de la propriété base64.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setBase64(String value) {
        this.base64 = value;
    }

    @Override
    public String toString() {
        return "DepotPESAllerStruct3{" +
                "filename='" + filename + '\'' +
                '}';
    }
}
