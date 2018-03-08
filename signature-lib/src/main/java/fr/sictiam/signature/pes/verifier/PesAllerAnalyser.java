package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.pes.CertificateContainer;
import fr.sictiam.signature.pes.verifier.SignatureTypeCalculator.SignatureAnexInfo1;
import fr.sictiam.signature.pes.verifier.SimplePesInformation.BordereauInfo1;
import fr.sictiam.signature.pes.verifier.SimplePesInformation.EntetePesInfo1;
import fr.sictiam.signature.utils.DomUtils;
import org.apache.xpath.XPathAPI;
import org.springframework.core.io.ClassPathResource;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.IOException;
import java.net.URL;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CRLException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PesAllerAnalyser {
    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);
    private boolean toImport;
    private ByteArrayInputStream pesSourceFile;
    private ByteArrayOutputStream pesDestinationFile;
    private Document pesDocument;
    private Node namespaceElement;
    private SimplePesInformation simplePesInformation;
    private SignatureVerifier signatureVerifier;
    private Map<Element, SignatureVerifierResult> signaturesVerifierResults;
    private boolean doSchemaValidation = false;
    private boolean schemaOK = false;
    private URL schemaUrl = null;
    private ArrayList<SAXParseException> saxParseExceptionsList;
    static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
    static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
    static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

    public PesAllerAnalyser(ByteArrayInputStream pesSourceFile, ByteArrayOutputStream pesDestinationFile) {
        this.pesSourceFile = pesSourceFile;
        this.pesDestinationFile = pesDestinationFile;
    }

    public PesAllerAnalyser(ByteArrayInputStream pesSourceFile) {
        this.pesSourceFile = pesSourceFile;
    }

    public PesAllerAnalyser(SimplePesInformation simplePesInformation) {
        this.simplePesInformation = simplePesInformation;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    public SimplePesInformation getSimplePesInformation() {
        return simplePesInformation;
    }

    public void setPesSourceFile(ByteArrayInputStream pesSourceFile) {
        ByteArrayInputStream old = this.pesSourceFile;
        this.pesSourceFile = pesSourceFile;
        pcs.firePropertyChange("pesSourceFile", old, pesSourceFile);
    }

    public ByteArrayOutputStream getPesDestinationFile() {
        return pesDestinationFile;
    }

    public void setPesDestinationFile(ByteArrayOutputStream pesDestinationFile) {
        ByteArrayOutputStream old = this.pesDestinationFile;
        this.pesDestinationFile = pesDestinationFile;
        pcs.firePropertyChange("pesDestinationFile", old, pesDestinationFile);
    }

    public boolean isToSign() {
        return true;
    }

    public boolean isToImport() {
        return toImport;
    }

    public void setToImport(boolean toImport) {
        this.toImport = toImport;
    }

    public void computeSimpleInformation() throws InvalidPesAllerFileException {
        try {
            SimplePesInformation workingSimplePesInformation = new SimplePesInformation();
            workingSimplePesInformation.setPesSourceFile(pesSourceFile);
            workingSimplePesInformation.setPesDestinationFile(pesDestinationFile);
            workingSimplePesInformation.setPesDocument(getDocument());
            NodeList sigs = XPathAPI.selectNodeList(getDocument(), "/n:PES_Aller/EnTetePES", getNamespaceNode());
            if (sigs.getLength() == 1) {
                Element element = (Element) sigs.item(0);
                workingSimplePesInformation
                        .setEntetePesInfo(new EntetePesInfo1(getFirstChildElementVAttribute(element, "DteStr"),
                                getFirstChildElementVAttribute(element, "IdPost"),
                                getFirstChildElementVAttribute(element, "IdColl"),
                                getFirstChildElementVAttribute(element, "CodCol"),
                                getFirstChildElementVAttribute(element, "CodBud"),
                                getFirstChildElementVAttribute(element, "LibelleColBud")));
            } else {
                if (sigs.getLength() < 1) {
                    throw new InvalidPesAllerFileException("données d'entête PES introuvable");
                }
                throw new InvalidPesAllerFileException("données d'entête PES incohérentes");
            }
            sigs = XPathAPI.selectNodeList(getDocument(), "/n:PES_Aller/PES_RecetteAller/Bordereau/BlocBordereau",
                    getNamespaceNode());
            for (int i = 0; i < sigs.getLength(); i++) {
                Element element = (Element) sigs.item(i);

                workingSimplePesInformation
                        .addBordereau(new BordereauInfo1(getFirstChildElementVAttribute(element, "IdBord"),
                                getFirstChildElementVAttribute(element, "Exer"),
                                getFirstChildElementVAttribute(element, "MtBordHT"), element.getAttribute("Id"),
                                ((Element) element.getParentNode()).getAttribute("Id")));
            }
            sigs = XPathAPI.selectNodeList(getDocument(), "/n:PES_Aller/PES_DepenseAller/Bordereau/BlocBordereau",
                    getNamespaceNode());
            for (int i = 0; i < sigs.getLength(); i++) {
                Element element = (Element) sigs.item(i);

                workingSimplePesInformation
                        .addBordereau(new BordereauInfo1(getFirstChildElementVAttribute(element, "IdBord"),
                                getFirstChildElementVAttribute(element, "Exer"),
                                getFirstChildElementVAttribute(element, "MtBordHT"), element.getAttribute("Id"),
                                ((Element) element.getParentNode()).getAttribute("Id")));
            }
            sigs = XPathAPI.selectNodeList(getDocument(), "//ds:Signature", getNamespaceNode());
            for (int i = 0; i < sigs.getLength(); i++) {
                Element element = (Element) sigs.item(i);
                workingSimplePesInformation.addSignatureElement(element);
            }
            GeneralSignaturePesData generalSignaturePesData = new GeneralSignaturePesData();
            Node nodeTmp = XPathAPI.selectSingleNode(getDocument(), "/n:PES_Aller/PES_DepenseAller/EnTeteDepense",
                    getNamespaceNode());
            if (nodeTmp != null) {
                generalSignaturePesData
                        .setEnteteDepenseValue(getFirstChildElementVAttribute((Element) nodeTmp, "InfoDematerialisee"));
            }
            nodeTmp = XPathAPI.selectSingleNode(getDocument(), "/n:PES_Aller/PES_RecetteAller/EnTeteRecette",
                    getNamespaceNode());
            if (nodeTmp != null) {
                generalSignaturePesData
                        .setEnTeteRecetteValue(getFirstChildElementVAttribute((Element) nodeTmp, "InfoDematerialisee"));
            }
            nodeTmp = XPathAPI.selectSingleNode(getDocument(), "/n:PES_Aller", getNamespaceNode());
            if (nodeTmp != null) {
                generalSignaturePesData.setIdPesAller(((Element) nodeTmp).getAttribute("Id"));
            }
            workingSimplePesInformation.getEntetePesInfo().setGeneralSignaturePesData(generalSignaturePesData);

            simplePesInformation = workingSimplePesInformation;
        } catch (CharConversionException cce) {
            throw new InvalidPesAllerFileException("Fichier Xml non valide", cce);
        } catch (SAXException saxe) {
            throw new InvalidPesAllerFileException("Fichier Xml non valide", saxe);
        } catch (TransformerException te) {
            throw new InvalidPesAllerFileException("Fichier Xml non valide", te);
        } catch (IOException ioe) {
            throw new UnExpectedException(ioe);
        } catch (ParserConfigurationException pce) {
            throw new UnExpectedException(pce);
        }
    }

    public Map<Element, SignatureVerifierResult> getSignaturesVerificationResults() {
        return signaturesVerifierResults;
    }

    public void computeSignaturesVerificationResults() throws InvalidPesAllerFileException {
        try {
            Map<Element, SignatureVerifierResult> workingSignatureVerifierResult = new HashMap<Element, SignatureVerifierResult>();
            for (Element signatureElement : getSimplePesInformation().getSignatureElements()) {
                SignatureVerifierResult signatureVerifierResult = getSignatureVerifier().process(signatureElement,
                        null);
                workingSignatureVerifierResult.put(signatureElement, signatureVerifierResult);
            }
            signaturesVerifierResults = workingSignatureVerifierResult;
        } catch (IOException ioe) {
            throw new UnExpectedException(ioe);
        } catch (CertificateException ce) {
            throw new UnExpectedException(ce);
        } catch (KeyStoreException kse) {
            throw new UnExpectedException(kse);
        } catch (CRLException crle) {
            throw new UnExpectedException(crle);
        } catch (NoSuchAlgorithmException nsae) {
            throw new UnExpectedException(nsae);
        } catch (InvalidAlgorithmParameterException iape) {
            throw new UnExpectedException(iape);
        }
    }

    public void computeSignaturesTypeVerification() {
        SignatureTypeCalculator signatureTypeCalculator = new SignatureTypeCalculator(simplePesInformation,
                signaturesVerifierResults);
        signatureTypeCalculator.verifAllBordereauxSigned();
        for (Element element : simplePesInformation.getSignatureElements()) {
            SignatureVerifierResult signatureVerifierResult = signaturesVerifierResults.get(element);
            if (signatureVerifierResult.getUnverifiableSignatureException() == null) {
                signatureVerifierResult
                        .setSignatureAnexInfo(new SignatureAnexInfo1(signatureTypeCalculator.process(element)));
                signatureVerifierResult
                        .setListeBordereauxNonSignes(signatureTypeCalculator.getListeBordereauxNonSignes());
                signatureVerifierResult
                        .setSignatureGlobalePresente(signatureTypeCalculator.isSignatureGlobalePresente());
            }
        }
    }

    public SignatureVerifier getSignatureVerifier() throws IOException, CertificateException, KeyStoreException,
            CRLException, NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        if (signatureVerifier == null) {
            ClassPathResource stream = new ClassPathResource("/signature/certs.zip");

            signatureVerifier = new SignatureVerifier(CertificateContainer.fromZipURL(stream.getInputStream()),
                    CertificateContainer.fromZipURL(stream.getInputStream()));
        }
        return signatureVerifier;
    }

    private Document getDocument() throws ParserConfigurationException, SAXException, IOException {
        ValidatorErrorHandler1 handler = null;
        if (pesDocument == null) {
            setSchemaOK(true);
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

            dbf.setAttribute("http://apache.org/xml/features/dom/defer-node-expansion", Boolean.TRUE);
            dbf.setValidating(false);
            dbf.setNamespaceAware(true);
            dbf.setAttribute("http://xml.org/sax/features/namespaces", Boolean.TRUE);
            DocumentBuilder db;
            if ((isDoSchemaValidation()) && (getSchemaUrl() != null)) {
                setSaxParseExceptionsList(new ArrayList<SAXParseException>());

                SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
                Schema schema = sf.newSchema(getSchemaUrl());

                dbf.setSchema(schema);

                db = dbf.newDocumentBuilder();
                handler = new ValidatorErrorHandler1();
                db.setErrorHandler(handler);
            } else {
                db = dbf.newDocumentBuilder();
            }
            pesDocument = db.parse(new InputSource(pesSourceFile));
            if ((isDoSchemaValidation()) && (getSchemaUrl() != null)) {
                setSchemaOK(handler.validationError);
            }
        }
        return pesDocument;
    }

    public Node getNamespaceNode() throws ParserConfigurationException, SAXException, IOException {
        if (namespaceElement == null) {
            namespaceElement = DomUtils.createXMLDSigAndPesNamespaceNode(getDocument());
        }
        return namespaceElement;
    }

    private String getFirstChildElementVAttribute(Element element, String elementName) {
        return DomUtils.getFCEA(element, elementName, "V");
    }

    public boolean isSchemaOK() {
        return schemaOK;
    }

    public void setSchemaOK(boolean schemaOK) {
        this.schemaOK = schemaOK;
    }

    public boolean isDoSchemaValidation() {
        return doSchemaValidation;
    }

    public void setDoSchemaValidation(boolean doSchemaValidation) {
        this.doSchemaValidation = doSchemaValidation;
    }

    public URL getSchemaUrl() {
        return schemaUrl;
    }

    public void setSchemaUrl(URL schemaUrl) {
        this.schemaUrl = schemaUrl;
    }

    public ArrayList<SAXParseException> getSaxParseExceptionsList() {
        return saxParseExceptionsList;
    }

    public void setSaxParseExceptionsList(ArrayList<SAXParseException> saxParseExceptionsList) {
        this.saxParseExceptionsList = saxParseExceptionsList;
    }

    private class ValidatorErrorHandler1 extends DefaultHandler {
        public boolean validationError = false;

        private ValidatorErrorHandler1() {
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            System.err.println("error :" + exception + " line " + exception.getLineNumber());
            if ((getSaxParseExceptionsList().isEmpty()) && (exception.getLineNumber() == 1)) {
                return;
            }
            validationError = true;
            getSaxParseExceptionsList().add(exception);
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            System.err.println("Fatal error :" + exception + " line " + exception.getLineNumber());

            validationError = true;
            getSaxParseExceptionsList().add(exception);
        }

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            System.err.println("Warning : " + exception + " line " + exception.getLineNumber());
        }
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.verifier.PesAllerAnalyser
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */