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
 * Classe Java pour getTabPESRetourStruct complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="getTabPESRetourStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="chaine_archive" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="filename" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getTabPESRetourStruct", propOrder = { "chaineArchive", "filename" })
public class GetTabPESRetourStruct {

    @XmlElement(name = "chaine_archive", required = true)
    protected String chaineArchive;
    @XmlElement(required = true)
    protected String filename;

    /**
     * Obtient la valeur de la propriété chaineArchive.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getChaineArchive() {
        return chaineArchive;
    }

    /**
     * Définit la valeur de la propriété chaineArchive.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setChaineArchive(String value) {
        this.chaineArchive = value;
    }

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

}
