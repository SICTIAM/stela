//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2017.08.08 at 03:18:09 PM CEST 
//

package fr.sictiam.stela.acteservice.model.xml;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.adapters.CollapsedStringAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import javax.xml.namespace.QName;

/**
 * This object contains factory methods for each Java content interface and Java
 * element interface generated in the fr.sictiam.stela.acteservice.model.xml
 * package.
 * <p>
 * An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content
 * can consist of schema derived interfaces and classes representing the binding
 * of schema type definitions, element declarations and model groups. Factory
 * methods for each of these are provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ARLettreObservations_QNAME = new QName(
            "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", "ARLettreObservations");
    private final static QName _NIC_QNAME = new QName("http://xml.insee.fr/schema", "NIC");
    private final static QName _SIRET_QNAME = new QName("http://xml.insee.fr/schema", "SIRET");
    private final static QName _Acte_QNAME = new QName("http://www.interieur.gouv.fr/ACTES#v1.1-20040216", "Acte");
    private final static QName _ARPieceComplementaire_QNAME = new QName(
            "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", "ARPieceComplementaire");
    private final static QName _ARReponseRejetLettreObservations_QNAME = new QName(
            "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", "ARReponseRejetLettreObservations");
    private final static QName _SIRETStructure_QNAME = new QName("http://xml.insee.fr/schema", "SIRETStructure");
    private final static QName _SIREN_QNAME = new QName("http://xml.insee.fr/schema", "SIREN");
    private final static QName _EnveloppeCLMISILL_QNAME = new QName("http://www.interieur.gouv.fr/ACTES#v1.1-20040216",
            "EnveloppeCLMISILL");
    private final static QName _ARDemandePieceComplementaire_QNAME = new QName(
            "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", "ARDemandePieceComplementaire");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema
     * derived classes for package: fr.sictiam.stela.acteservice.model.xml
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link EnveloppeMISILLCL }
     * 
     */
    public EnveloppeMISILLCL createEnveloppeMISILLCL() {
        return new EnveloppeMISILLCL();
    }

    /**
     * Create an instance of {@link RetourClassification }
     * 
     */
    public RetourClassification createRetourClassification() {
        return new RetourClassification();
    }

    /**
     * Create an instance of {@link DefereTA }
     * 
     */
    public DefereTA createDefereTA() {
        return new DefereTA();
    }

    /**
     * Create an instance of {@link RetourClassification.Matieres }
     * 
     */
    public RetourClassification.Matieres createRetourClassificationMatieres() {
        return new RetourClassification.Matieres();
    }

    /**
     * Create an instance of {@link RetourClassification.Matieres.Matiere1 }
     * 
     */
    public RetourClassification.Matieres.Matiere1 createRetourClassificationMatieresMatiere1() {
        return new RetourClassification.Matieres.Matiere1();
    }

    /**
     * Create an instance of
     * {@link RetourClassification.Matieres.Matiere1 .Matiere2 }
     * 
     */
    public RetourClassification.Matieres.Matiere1.Matiere2 createRetourClassificationMatieresMatiere1Matiere2() {
        return new RetourClassification.Matieres.Matiere1.Matiere2();
    }

    /**
     * Create an instance of
     * {@link RetourClassification.Matieres.Matiere1 .Matiere2 .Matiere3 }
     * 
     */
    public RetourClassification.Matieres.Matiere1.Matiere2.Matiere3 createRetourClassificationMatieresMatiere1Matiere2Matiere3() {
        return new RetourClassification.Matieres.Matiere1.Matiere2.Matiere3();
    }

    /**
     * Create an instance of {@link RetourClassification.NaturesActes }
     * 
     */
    public RetourClassification.NaturesActes createRetourClassificationNaturesActes() {
        return new RetourClassification.NaturesActes();
    }

    /**
     * Create an instance of {@link DonneesActe }
     * 
     */
    public DonneesActe createDonneesActe() {
        return new DonneesActe();
    }

    /**
     * Create an instance of {@link DonneesEnveloppeCLMISILL }
     * 
     */
    public DonneesEnveloppeCLMISILL createDonneesEnveloppeCLMISILL() {
        return new DonneesEnveloppeCLMISILL();
    }

    /**
     * Create an instance of {@link Referent }
     * 
     */
    public Referent createReferent() {
        return new Referent();
    }

    /**
     * Create an instance of {@link DemandeClassification }
     * 
     */
    public DemandeClassification createDemandeClassification() {
        return new DemandeClassification();
    }

    /**
     * Create an instance of {@link AnomalieEnveloppe }
     * 
     */
    public AnomalieEnveloppe createAnomalieEnveloppe() {
        return new AnomalieEnveloppe();
    }

    /**
     * Create an instance of {@link AnomalieActe }
     * 
     */
    public AnomalieActe createAnomalieActe() {
        return new AnomalieActe();
    }

    /**
     * Create an instance of {@link ReponseCourrierSimple }
     * 
     */
    public ReponseCourrierSimple createReponseCourrierSimple() {
        return new ReponseCourrierSimple();
    }

    /**
     * Create an instance of {@link DonneesCourrierPref }
     * 
     */
    public DonneesCourrierPref createDonneesCourrierPref() {
        return new DonneesCourrierPref();
    }

    /**
     * Create an instance of {@link FichierSigne }
     * 
     */
    public FichierSigne createFichierSigne() {
        return new FichierSigne();
    }

    /**
     * Create an instance of {@link IDSGAR }
     * 
     */
    public IDSGAR createIDSGAR() {
        return new IDSGAR();
    }

    /**
     * Create an instance of {@link FormulairesEnvoyes }
     * 
     */
    public FormulairesEnvoyes createFormulairesEnvoyes() {
        return new FormulairesEnvoyes();
    }

    /**
     * Create an instance of {@link LettreObservations }
     * 
     */
    public LettreObservations createLettreObservations() {
        return new LettreObservations();
    }

    /**
     * Create an instance of {@link ARReponseCL }
     * 
     */
    public ARReponseCL createARReponseCL() {
        return new ARReponseCL();
    }

    /**
     * Create an instance of {@link IDCL }
     * 
     */
    public IDCL createIDCL() {
        return new IDCL();
    }

    /**
     * Create an instance of {@link EnveloppeMISILLCL.Emetteur }
     * 
     */
    public EnveloppeMISILLCL.Emetteur createEnveloppeMISILLCLEmetteur() {
        return new EnveloppeMISILLCL.Emetteur();
    }

    /**
     * Create an instance of {@link EnveloppeMISILLCL.Destinataire }
     * 
     */
    public EnveloppeMISILLCL.Destinataire createEnveloppeMISILLCLDestinataire() {
        return new EnveloppeMISILLCL.Destinataire();
    }

    /**
     * Create an instance of {@link IDPref }
     * 
     */
    public IDPref createIDPref() {
        return new IDPref();
    }

    /**
     * Create an instance of {@link ARActe }
     * 
     */
    public ARActe createARActe() {
        return new ARActe();
    }

    /**
     * Create an instance of {@link ReponseClassificationSansChangement }
     * 
     */
    public ReponseClassificationSansChangement createReponseClassificationSansChangement() {
        return new ReponseClassificationSansChangement();
    }

    /**
     * Create an instance of {@link RejetLettreObservations }
     * 
     */
    public RejetLettreObservations createRejetLettreObservations() {
        return new RejetLettreObservations();
    }

    /**
     * Create an instance of {@link IDSousPref }
     * 
     */
    public IDSousPref createIDSousPref() {
        return new IDSousPref();
    }

    /**
     * Create an instance of {@link ReponseLettreObservations }
     * 
     */
    public ReponseLettreObservations createReponseLettreObservations() {
        return new ReponseLettreObservations();
    }

    /**
     * Create an instance of {@link ARAnnulation }
     * 
     */
    public ARAnnulation createARAnnulation() {
        return new ARAnnulation();
    }

    /**
     * Create an instance of {@link RefusPieceComplementaire }
     * 
     */
    public RefusPieceComplementaire createRefusPieceComplementaire() {
        return new RefusPieceComplementaire();
    }

    /**
     * Create an instance of {@link DemandePieceComplementaire }
     * 
     */
    public DemandePieceComplementaire createDemandePieceComplementaire() {
        return new DemandePieceComplementaire();
    }

    /**
     * Create an instance of {@link DefereTA.PiecesJointes }
     * 
     */
    public DefereTA.PiecesJointes createDefereTAPiecesJointes() {
        return new DefereTA.PiecesJointes();
    }

    /**
     * Create an instance of {@link PieceComplementaire }
     * 
     */
    public PieceComplementaire createPieceComplementaire() {
        return new PieceComplementaire();
    }

    /**
     * Create an instance of {@link FichiersSignes }
     * 
     */
    public FichiersSignes createFichiersSignes() {
        return new FichiersSignes();
    }

    /**
     * Create an instance of {@link CourrierSimple }
     * 
     */
    public CourrierSimple createCourrierSimple() {
        return new CourrierSimple();
    }

    /**
     * Create an instance of {@link Annulation }
     * 
     */
    public Annulation createAnnulation() {
        return new Annulation();
    }

    /**
     * Create an instance of {@link Matiere }
     * 
     */
    public Matiere createMatiere() {
        return new Matiere();
    }

    /**
     * Create an instance of {@link TypePieceJointe }
     * 
     */
    public TypePieceJointe createTypePieceJointe() {
        return new TypePieceJointe();
    }

    /**
     * Create an instance of {@link SIRETStructureType }
     * 
     */
    public SIRETStructureType createSIRETStructureType() {
        return new SIRETStructureType();
    }

    /**
     * Create an instance of {@link SIRETCtype }
     * 
     */
    public SIRETCtype createSIRETCtype() {
        return new SIRETCtype();
    }

    /**
     * Create an instance of
     * {@link RetourClassification.Matieres.Matiere1 .Matiere2 .Matiere3 .Matiere4 }
     * 
     */
    public RetourClassification.Matieres.Matiere1.Matiere2.Matiere3.Matiere4 createRetourClassificationMatieresMatiere1Matiere2Matiere3Matiere4() {
        return new RetourClassification.Matieres.Matiere1.Matiere2.Matiere3.Matiere4();
    }

    /**
     * Create an instance of {@link RetourClassification.NaturesActes.NatureActe }
     * 
     */
    public RetourClassification.NaturesActes.NatureActe createRetourClassificationNaturesActesNatureActe() {
        return new RetourClassification.NaturesActes.NatureActe();
    }

    /**
     * Create an instance of {@link DonneesActe.CodeMatiere1 }
     * 
     */
    public DonneesActe.CodeMatiere1 createDonneesActeCodeMatiere1() {
        return new DonneesActe.CodeMatiere1();
    }

    /**
     * Create an instance of {@link DonneesActe.CodeMatiere2 }
     * 
     */
    public DonneesActe.CodeMatiere2 createDonneesActeCodeMatiere2() {
        return new DonneesActe.CodeMatiere2();
    }

    /**
     * Create an instance of {@link DonneesActe.CodeMatiere3 }
     * 
     */
    public DonneesActe.CodeMatiere3 createDonneesActeCodeMatiere3() {
        return new DonneesActe.CodeMatiere3();
    }

    /**
     * Create an instance of {@link DonneesActe.CodeMatiere4 }
     * 
     */
    public DonneesActe.CodeMatiere4 createDonneesActeCodeMatiere4() {
        return new DonneesActe.CodeMatiere4();
    }

    /**
     * Create an instance of {@link DonneesActe.CodeMatiere5 }
     * 
     */
    public DonneesActe.CodeMatiere5 createDonneesActeCodeMatiere5() {
        return new DonneesActe.CodeMatiere5();
    }

    /**
     * Create an instance of {@link DonneesActe.PrecedentIdentifiantActe }
     * 
     */
    public DonneesActe.PrecedentIdentifiantActe createDonneesActePrecedentIdentifiantActe() {
        return new DonneesActe.PrecedentIdentifiantActe();
    }

    /**
     * Create an instance of {@link DonneesActe.Annexes }
     * 
     */
    public DonneesActe.Annexes createDonneesActeAnnexes() {
        return new DonneesActe.Annexes();
    }

    /**
     * Create an instance of {@link DonneesEnveloppeCLMISILL.Emetteur }
     * 
     */
    public DonneesEnveloppeCLMISILL.Emetteur createDonneesEnveloppeCLMISILLEmetteur() {
        return new DonneesEnveloppeCLMISILL.Emetteur();
    }

    /**
     * Create an instance of {@link DonneesEnveloppeCLMISILL.AdressesRetour }
     * 
     */
    public DonneesEnveloppeCLMISILL.AdressesRetour createDonneesEnveloppeCLMISILLAdressesRetour() {
        return new DonneesEnveloppeCLMISILL.AdressesRetour();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DonneesCourrierPref
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", name = "ARLettreObservations")
    public JAXBElement<DonneesCourrierPref> createARLettreObservations(DonneesCourrierPref value) {
        return new JAXBElement<DonneesCourrierPref>(_ARLettreObservations_QNAME, DonneesCourrierPref.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.insee.fr/schema", name = "NIC")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createNIC(String value) {
        return new JAXBElement<String>(_NIC_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.insee.fr/schema", name = "SIRET")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createSIRET(String value) {
        return new JAXBElement<String>(_SIRET_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DonneesActe
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", name = "Acte")
    public JAXBElement<DonneesActe> createActe(DonneesActe value) {
        return new JAXBElement<DonneesActe>(_Acte_QNAME, DonneesActe.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ARReponseCL
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", name = "ARPieceComplementaire")
    public JAXBElement<ARReponseCL> createARPieceComplementaire(ARReponseCL value) {
        return new JAXBElement<ARReponseCL>(_ARPieceComplementaire_QNAME, ARReponseCL.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ARReponseCL
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", name = "ARReponseRejetLettreObservations")
    public JAXBElement<ARReponseCL> createARReponseRejetLettreObservations(ARReponseCL value) {
        return new JAXBElement<ARReponseCL>(_ARReponseRejetLettreObservations_QNAME, ARReponseCL.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link SIRETStructureType
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.insee.fr/schema", name = "SIRETStructure")
    public JAXBElement<SIRETStructureType> createSIRETStructure(SIRETStructureType value) {
        return new JAXBElement<SIRETStructureType>(_SIRETStructure_QNAME, SIRETStructureType.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://xml.insee.fr/schema", name = "SIREN")
    @XmlJavaTypeAdapter(CollapsedStringAdapter.class)
    public JAXBElement<String> createSIREN(String value) {
        return new JAXBElement<String>(_SIREN_QNAME, String.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement
     * }{@code <}{@link DonneesEnveloppeCLMISILL }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", name = "EnveloppeCLMISILL")
    public JAXBElement<DonneesEnveloppeCLMISILL> createEnveloppeCLMISILL(DonneesEnveloppeCLMISILL value) {
        return new JAXBElement<DonneesEnveloppeCLMISILL>(_EnveloppeCLMISILL_QNAME, DonneesEnveloppeCLMISILL.class, null,
                value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link DonneesCourrierPref
     * }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://www.interieur.gouv.fr/ACTES#v1.1-20040216", name = "ARDemandePieceComplementaire")
    public JAXBElement<DonneesCourrierPref> createARDemandePieceComplementaire(DonneesCourrierPref value) {
        return new JAXBElement<DonneesCourrierPref>(_ARDemandePieceComplementaire_QNAME, DonneesCourrierPref.class,
                null, value);
    }

}
