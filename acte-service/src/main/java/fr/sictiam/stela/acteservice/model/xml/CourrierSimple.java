//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.08.08 at 03:18:09 PM CEST 
//


package fr.sictiam.stela.acteservice.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://www.interieur.gouv.fr/ACTES#v1.1-20040216}DonneesCourrierPref">
 *       &lt;sequence>
 *         &lt;element name="Document" type="{http://www.interieur.gouv.fr/ACTES#v1.1-20040216}FichierSigne"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "document"
})
@XmlRootElement(name = "CourrierSimple")
public class CourrierSimple
    extends DonneesCourrierPref
{

    @XmlElement(name = "Document", required = true)
    protected FichierSigne document;

    /**
     * Gets the value of the document property.
     * 
     * @return
     *     possible object is
     *     {@link FichierSigne }
     *     
     */
    public FichierSigne getDocument() {
        return document;
    }

    /**
     * Sets the value of the document property.
     * 
     * @param value
     *     allowed object is
     *     {@link FichierSigne }
     *     
     */
    public void setDocument(FichierSigne value) {
        this.document = value;
    }

}
