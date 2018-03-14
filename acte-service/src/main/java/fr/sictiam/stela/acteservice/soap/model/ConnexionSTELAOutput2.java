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
 * Classe Java pour connexionSTELA_Output2 complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="connexionSTELA_Output2"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="jsonConnexionSTELA2" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "connexionSTELA_Output2", propOrder = { "jsonConnexionSTELA2" })
@XmlRootElement(name = "setConnexionSTELA2")
public class ConnexionSTELAOutput2 {

    @XmlElement(required = true)
    protected String jsonConnexionSTELA2;

    /**
     * Obtient la valeur de la propriété jsonConnexionSTELA2.
     *
     * @return possible object is {@link String }
     *
     */
    public String getJsonConnexionSTELA2() {
        return jsonConnexionSTELA2;
    }

    /**
     * Définit la valeur de la propriété jsonConnexionSTELA2.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setJsonConnexionSTELA2(String value) {
        this.jsonConnexionSTELA2 = value;
    }

}
