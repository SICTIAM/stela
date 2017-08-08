//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.08.08 at 03:18:09 PM CEST 
//


package fr.sictiam.stela.acteservice.model.xml;

import java.time.LocalDate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Données d'un AR d'une réponse d'une collectivité au courrier d'une préfecture (envoi ou refus d'envoyer des pièces complémentaires, réponse ou rejet pour une lettre d'observations)
 * 
 * <p>Java class for ARReponseCL complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ARReponseCL">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="InfosCourrierPref" type="{http://www.interieur.gouv.fr/ACTES#v1.1-20040216}DonneesCourrierPref"/>
 *       &lt;/sequence>
 *       &lt;attribute name="DateReception" use="required" type="{http://www.w3.org/2001/XMLSchema}date" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ARReponseCL", propOrder = {
    "infosCourrierPref"
})
public class ARReponseCL {

    @XmlElement(name = "InfosCourrierPref", required = true)
    protected DonneesCourrierPref infosCourrierPref;
    @XmlAttribute(name = "DateReception", namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", required = true)
    @XmlJavaTypeAdapter(Adapter1 .class)
    @XmlSchemaType(name = "date")
    protected LocalDate dateReception;

    /**
     * Gets the value of the infosCourrierPref property.
     * 
     * @return
     *     possible object is
     *     {@link DonneesCourrierPref }
     *     
     */
    public DonneesCourrierPref getInfosCourrierPref() {
        return infosCourrierPref;
    }

    /**
     * Sets the value of the infosCourrierPref property.
     * 
     * @param value
     *     allowed object is
     *     {@link DonneesCourrierPref }
     *     
     */
    public void setInfosCourrierPref(DonneesCourrierPref value) {
        this.infosCourrierPref = value;
    }

    /**
     * Gets the value of the dateReception property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LocalDate getDateReception() {
        return dateReception;
    }

    /**
     * Sets the value of the dateReception property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDateReception(LocalDate value) {
        this.dateReception = value;
    }

}
