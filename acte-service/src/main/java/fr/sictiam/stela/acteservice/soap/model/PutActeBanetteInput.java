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
 * Classe Java pour PutActe_Banette_Input complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="PutActe_Banette_Input"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="file_content" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="mail" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="pwd" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PutActe_Banette_Input", propOrder = { "fileContent", "mail", "pwd" })
@XmlRootElement(name = "getPutActe_Banette")
public class PutActeBanetteInput {

    @XmlElement(name = "file_content", required = true)
    protected String fileContent;
    @XmlElement(required = true)
    protected String mail;
    @XmlElement(required = true)
    protected String pwd;

    /**
     * Obtient la valeur de la propriété fileContent.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFileContent() {
        return fileContent;
    }

    /**
     * Définit la valeur de la propriété fileContent.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFileContent(String value) {
        this.fileContent = value;
    }

    /**
     * Obtient la valeur de la propriété mail.
     *
     * @return possible object is {@link String }
     *
     */
    public String getMail() {
        return mail;
    }

    /**
     * Définit la valeur de la propriété mail.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setMail(String value) {
        this.mail = value;
    }

    /**
     * Obtient la valeur de la propriété pwd.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPwd() {
        return pwd;
    }

    /**
     * Définit la valeur de la propriété pwd.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setPwd(String value) {
        this.pwd = value;
    }

}
