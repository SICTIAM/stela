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
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Classe Java pour GetListeDeliberations_Input complex type.
 *
 * <p>
 * Le fragment de schéma suivant indique le contenu attendu figurant dans cette
 * classe.
 *
 * <pre>
 * &lt;complexType name="GetListeDeliberations_Input"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="groupe" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="start" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="perPage" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="sort" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="order" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="champRecherche" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="filtreAnnee" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="lienDetail" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="filtreNature" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 *
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GetListeDeliberations_Input", propOrder = { "groupe", "start", "perPage", "sort", "order",
        "champRecherche", "filtreAnnee", "lienDetail", "filtreNature" })
public class GetListeDeliberationsInput {

    @XmlElement(required = true)
    protected String groupe;
    @XmlElement(required = true)
    protected String start;
    @XmlElement(required = true)
    protected String perPage;
    @XmlElement(required = true)
    protected String sort;
    @XmlElement(required = true)
    protected String order;
    @XmlElement(required = true)
    protected String champRecherche;
    @XmlElement(required = true)
    protected String filtreAnnee;
    @XmlElement(required = true)
    protected String lienDetail;
    @XmlElement(required = true)
    protected String filtreNature;

    /**
     * Obtient la valeur de la propriété groupe.
     *
     * @return possible object is {@link String }
     *
     */
    public String getGroupe() {
        return groupe;
    }

    /**
     * Définit la valeur de la propriété groupe.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setGroupe(String value) {
        this.groupe = value;
    }

    /**
     * Obtient la valeur de la propriété start.
     *
     * @return possible object is {@link String }
     *
     */
    public String getStart() {
        return start;
    }

    /**
     * Définit la valeur de la propriété start.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setStart(String value) {
        this.start = value;
    }

    /**
     * Obtient la valeur de la propriété perPage.
     *
     * @return possible object is {@link String }
     *
     */
    public String getPerPage() {
        return perPage;
    }

    /**
     * Définit la valeur de la propriété perPage.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setPerPage(String value) {
        this.perPage = value;
    }

    /**
     * Obtient la valeur de la propriété sort.
     *
     * @return possible object is {@link String }
     *
     */
    public String getSort() {
        return sort;
    }

    /**
     * Définit la valeur de la propriété sort.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setSort(String value) {
        this.sort = value;
    }

    /**
     * Obtient la valeur de la propriété order.
     *
     * @return possible object is {@link String }
     *
     */
    public String getOrder() {
        return order;
    }

    /**
     * Définit la valeur de la propriété order.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setOrder(String value) {
        this.order = value;
    }

    /**
     * Obtient la valeur de la propriété champRecherche.
     *
     * @return possible object is {@link String }
     *
     */
    public String getChampRecherche() {
        return champRecherche;
    }

    /**
     * Définit la valeur de la propriété champRecherche.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setChampRecherche(String value) {
        this.champRecherche = value;
    }

    /**
     * Obtient la valeur de la propriété filtreAnnee.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFiltreAnnee() {
        return filtreAnnee;
    }

    /**
     * Définit la valeur de la propriété filtreAnnee.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFiltreAnnee(String value) {
        this.filtreAnnee = value;
    }

    /**
     * Obtient la valeur de la propriété lienDetail.
     *
     * @return possible object is {@link String }
     *
     */
    public String getLienDetail() {
        return lienDetail;
    }

    /**
     * Définit la valeur de la propriété lienDetail.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setLienDetail(String value) {
        this.lienDetail = value;
    }

    /**
     * Obtient la valeur de la propriété filtreNature.
     *
     * @return possible object is {@link String }
     *
     */
    public String getFiltreNature() {
        return filtreNature;
    }

    /**
     * Définit la valeur de la propriété filtreNature.
     *
     * @param value
     *            allowed object is {@link String }
     *
     */
    public void setFiltreNature(String value) {
        this.filtreNature = value;
    }

}
