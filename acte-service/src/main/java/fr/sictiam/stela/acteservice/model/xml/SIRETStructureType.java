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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for SIRETStructureType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="SIRETStructureType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element ref="{http://xml.insee.fr/schema}SIREN"/>
 *         &lt;element ref="{http://xml.insee.fr/schema}NIC"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SIRETStructureType", namespace = "http://xml.insee.fr/schema", propOrder = {
    "siren",
    "nic"
})
public class SIRETStructureType {

    @XmlElement(name = "SIREN", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String siren;
    @XmlElement(name = "NIC", required = true)
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    @XmlSchemaType(name = "token")
    protected String nic;

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
     * Gets the value of the nic property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNIC() {
        return nic;
    }

    /**
     * Sets the value of the nic property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNIC(String value) {
        this.nic = value;
    }

}