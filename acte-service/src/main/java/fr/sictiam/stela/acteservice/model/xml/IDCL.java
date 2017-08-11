//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.08.08 at 03:18:09 PM CEST 
//


package fr.sictiam.stela.acteservice.model.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;attribute ref="{http://xml.insee.fr/schema}SIREN use="required""/>
 *       &lt;attribute ref="{http://www.interieur.gouv.fr/ACTES#v1.1-20040216}Departement use="required""/>
 *       &lt;attribute ref="{http://www.interieur.gouv.fr/ACTES#v1.1-20040216}Arrondissement use="required""/>
 *       &lt;attribute name="Nature" use="required">
 *         &lt;simpleType>
 *           &lt;restriction base="{http://www.w3.org/2001/XMLSchema}nonNegativeInteger">
 *             &lt;pattern value="[0-9]{2}"/>
 *           &lt;/restriction>
 *         &lt;/simpleType>
 *       &lt;/attribute>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "IDCL")
public class IDCL {

    @XmlAttribute(name = "SIREN", namespace = "http://xml.insee.fr/schema", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    protected String siren;
    @XmlAttribute(name = "Departement", namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", required = true)
    protected String departement;
    @XmlAttribute(name = "Arrondissement", namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", required = true)
    @XmlJavaTypeAdapter(Adapter2 .class)
    protected String arrondissement;
    @XmlAttribute(name = "Nature", namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", required = true)
    @XmlJavaTypeAdapter(Adapter2 .class)
    protected String nature;

    /**
     * Gets the value of the siren property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSIREN() {
        return siren;
    }

    /**
     * Sets the value of the siren property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSIREN(String value) {
        this.siren = value;
    }

    /**
     * Gets the value of the departement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDepartement() {
        return departement;
    }

    /**
     * Sets the value of the departement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDepartement(String value) {
        this.departement = value;
    }

    /**
     * Gets the value of the arrondissement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getArrondissement() {
        return arrondissement;
    }

    /**
     * Sets the value of the arrondissement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setArrondissement(String value) {
        this.arrondissement = value;
    }

    /**
     * Gets the value of the nature property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNature() {
        return nature;
    }

    /**
     * Sets the value of the nature property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNature(String value) {
        this.nature = value;
    }

}