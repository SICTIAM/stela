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
 * Classe Java pour depotPESAllerStruct1 complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="depotPESAllerStruct1"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="title" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="comment" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="name" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="desc" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="validation" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="PESPJ" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="email" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="SSLSerial" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="SSLVendor" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="token" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="secret" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="tokenSign" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="secretSign" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="groupid" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "depotPESAllerStruct1", propOrder = { "title", "comment", "name", "desc", "validation", "pespj",
        "email", "sslSerial", "sslVendor", "token", "secret", "tokenSign", "secretSign", "groupid" })
public class DepotPESAllerStruct1 {

    @XmlElement(required = true)
    protected String title;
    @XmlElement(required = true)
    protected String comment;
    @XmlElement(required = true)
    protected String name;
    @XmlElement(required = true)
    protected String desc;
    @XmlElement(required = true)
    protected String validation;
    @XmlElement(name = "PESPJ", required = true, type = Integer.class, defaultValue = "0", nillable = true)
    protected Integer pespj;
    @XmlElement(required = true, nillable = true)
    protected String email;
    @XmlElement(name = "SSLSerial", required = true, nillable = true)
    protected String sslSerial;
    @XmlElement(name = "SSLVendor", required = true, nillable = true)
    protected String sslVendor;
    @XmlElement(required = true, nillable = true)
    protected String token;
    @XmlElement(required = true, nillable = true)
    protected String secret;
    @XmlElement(required = true, nillable = true)
    protected String tokenSign;
    @XmlElement(required = true, nillable = true)
    protected String secretSign;
    @XmlElement(required = true, nillable = true)
    protected String groupid;

    /**
     * Obtient la valeur de la propriété title.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTitle() {
        return title;
    }

    /**
     * Définit la valeur de la propriété title.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setTitle(String value) {
        this.title = value;
    }

    /**
     * Obtient la valeur de la propriété comment.
     *
     * @return possible object is {@link String }
     *
     */
    public String getComment() {
        return comment;
    }

    /**
     * Définit la valeur de la propriété comment.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setComment(String value) {
        this.comment = value;
    }

    /**
     * Obtient la valeur de la propriété name.
     *
     * @return possible object is {@link String }
     *
     */
    public String getName() {
        return name;
    }

    /**
     * Définit la valeur de la propriété name.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Obtient la valeur de la propriété desc.
     *
     * @return possible object is {@link String }
     *
     */
    public String getDesc() {
        return desc;
    }

    /**
     * Définit la valeur de la propriété desc.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setDesc(String value) {
        this.desc = value;
    }

    /**
     * Obtient la valeur de la propriété validation.
     *
     * @return possible object is {@link String }
     *
     */
    public String getValidation() {
        return validation;
    }

    /**
     * Définit la valeur de la propriété validation.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setValidation(String value) {
        this.validation = value;
    }

    /**
     * Obtient la valeur de la propriété pespj.
     *
     * @return possible object is {@link Integer }
     *
     */
    public Integer getPESPJ() {
        return pespj;
    }

    /**
     * Définit la valeur de la propriété pespj.
     *
     * @param value
     *            allowed object is {@link Integer }
     *
     */
    public void setPESPJ(Integer value) {
        this.pespj = value;
    }

    /**
     * Obtient la valeur de la propriété email.
     *
     * @return possible object is {@link String }
     *
     */
    public String getEmail() {
        return email;
    }

    /**
     * Définit la valeur de la propriété email.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setEmail(String value) {
        this.email = value;
    }

    /**
     * Obtient la valeur de la propriété sslSerial.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSSLSerial() {
        return sslSerial;
    }

    /**
     * Définit la valeur de la propriété sslSerial.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSSLSerial(String value) {
        this.sslSerial = value;
    }

    /**
     * Obtient la valeur de la propriété sslVendor.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSSLVendor() {
        return sslVendor;
    }

    /**
     * Définit la valeur de la propriété sslVendor.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSSLVendor(String value) {
        this.sslVendor = value;
    }

    /**
     * Obtient la valeur de la propriété token.
     *
     * @return possible object is {@link String }
     *
     */
    public String getToken() {
        return token;
    }

    /**
     * Définit la valeur de la propriété token.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setToken(String value) {
        this.token = value;
    }

    /**
     * Obtient la valeur de la propriété secret.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSecret() {
        return secret;
    }

    /**
     * Définit la valeur de la propriété secret.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSecret(String value) {
        this.secret = value;
    }

    /**
     * Obtient la valeur de la propriété tokenSign.
     *
     * @return possible object is {@link String }
     *
     */
    public String getTokenSign() {
        return tokenSign;
    }

    /**
     * Définit la valeur de la propriété tokenSign.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setTokenSign(String value) {
        this.tokenSign = value;
    }

    /**
     * Obtient la valeur de la propriété secretSign.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSecretSign() {
        return secretSign;
    }

    /**
     * Définit la valeur de la propriété secretSign.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSecretSign(String value) {
        this.secretSign = value;
    }

    /**
     * Obtient la valeur de la propriété groupid.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGroupid() {
        return groupid;
    }

    /**
     * Définit la valeur de la propriété groupid.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setGroupid(String value) {
        this.groupid = value;
    }

    @Override
    public String toString() {
        return "DepotPESAllerStruct1 [title=" + title + ", comment=" + comment + ", name=" + name + ", desc=" + desc
                + ", validation=" + validation + ", pespj=" + pespj + ", email=" + email + ", sslSerial=" + sslSerial
                + ", sslVendor=" + sslVendor + ", token=" + token + ", secret=" + secret + ", tokenSign=" + tokenSign
                + ", secretSign=" + secretSign + ", groupid=" + groupid + "]";
    }

}
