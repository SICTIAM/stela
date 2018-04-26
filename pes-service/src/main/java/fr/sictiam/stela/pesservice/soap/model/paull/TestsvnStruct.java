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
 * Classe Java pour testsvnStruct complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="testsvnStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="result1" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="result2" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "testsvnStruct", propOrder = { "result1", "result2" })
public class TestsvnStruct {

    @XmlElement(required = true)
    protected String result1;
    @XmlElement(required = true)
    protected String result2;

    /**
     * Obtient la valeur de la propriété result1.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getResult1() {
        return result1;
    }

    /**
     * Définit la valeur de la propriété result1.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setResult1(String value) {
        this.result1 = value;
    }

    /**
     * Obtient la valeur de la propriété result2.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getResult2() {
        return result2;
    }

    /**
     * Définit la valeur de la propriété result2.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setResult2(String value) {
        this.result2 = value;
    }

}
