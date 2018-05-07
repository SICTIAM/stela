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

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * Classe Java pour getPESRetourStruct complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="getPESRetourStruct"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tabpesretour" type="{http://www.processmaker.com}getTabPESRetourStruct" maxOccurs="unbounded"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPESRetourStruct", propOrder = { "message", "tabpesretour" })
public class GetPESRetourStruct {

    @XmlElement(required = true)
    protected String message;
    @XmlElement(required = true)
    protected List<GetTabPESRetourStruct> tabpesretour;

    /**
     * Obtient la valeur de la propriété message.
     *
     * @return possible object is {@link String }
     * 
     */
    public String getMessage() {
        return message;
    }

    /**
     * Définit la valeur de la propriété message.
     *
     * @param value
     *            allowed object is {@link String }
     * 
     */
    public void setMessage(String value) {
        this.message = value;
    }

    /**
     * Gets the value of the tabpesretour property.
     *
     * <p>
     * This accessor method returns a reference to the live list, not a snapshot.
     * Therefore any modification you make to the returned list will be present
     * inside the JAXB object. This is why there is not a <CODE>set</CODE> method
     * for the tabpesretour property.
     *
     * <p>
     * For example, to add a new item, do as follows:
     * 
     * <pre>
     * getTabpesretour().add(newItem);
     * </pre>
     *
     *
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link GetTabPESRetourStruct }
     *
     *
     */
    public List<GetTabPESRetourStruct> getTabpesretour() {
        if (tabpesretour == null) {
            tabpesretour = new ArrayList<GetTabPESRetourStruct>();
        }
        return this.tabpesretour;
    }

}
