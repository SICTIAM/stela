package fr.sictiam.signature.pes.producer;

import fr.sictiam.signature.pes.producer.AbstractKeyStoreLoader.KeyAliases1;
import fr.sictiam.signature.pes.producer.SigningPolicies.SigningPolicy1;
import fr.sictiam.signature.pes.verifier.DigestUtils;
import fr.sictiam.signature.pes.verifier.SimplePesInformation;
import fr.sictiam.signature.pes.verifier.XadesInfoExtractor.XadesInfo1;
import fr.sictiam.signature.utils.DateUtils;
import fr.sictiam.signature.utils.DomUtils;
import fr.sictiam.signature.utils.DomUtils.ElementAttribute1;
import org.apache.xml.security.Init;
import org.apache.xml.security.exceptions.XMLSecurityException;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.crypto.MarshalException;
import javax.xml.crypto.dom.DOMStructure;
import javax.xml.crypto.dsig.Reference;
import javax.xml.crypto.dsig.SignedInfo;
import javax.xml.crypto.dsig.Transform;
import javax.xml.crypto.dsig.XMLObject;
import javax.xml.crypto.dsig.XMLSignature;
import javax.xml.crypto.dsig.XMLSignatureException;
import javax.xml.crypto.dsig.XMLSignatureFactory;
import javax.xml.crypto.dsig.dom.DOMSignContext;
import javax.xml.crypto.dsig.keyinfo.KeyInfo;
import javax.xml.crypto.dsig.keyinfo.KeyInfoFactory;
import javax.xml.crypto.dsig.keyinfo.X509Data;
import javax.xml.crypto.dsig.spec.C14NMethodParameterSpec;
import javax.xml.crypto.dsig.spec.TransformParameterSpec;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class SignatureGenerator {

    static {
        Init.init();
    }

    private static String DEUX_POINT = ":";
    private static String prefixEtsi;
    private static String prefixXmlDsig;

    private static String getSignatureID(int signatureOrder) {
        String rawS = "IDC" + Integer.toString(signatureOrder)
                + new SimpleDateFormat("yyMMddHHmmssZ").format(new Date());
        rawS = rawS.replace('-', 'm');
        rawS = rawS.replace('+', 'p');
        return rawS;
    }

    public static void createSignature(SimplePesInformation simplePesInformation, KeyAliases1 keyAlias,
            SigningPolicy1 signingPolicy, XadesInfo1 xadesInfo)
            throws XMLSecurityException, InvalidAlgorithmParameterException, TransformerException,
            ParserConfigurationException, SAXException, IOException, CertificateEncodingException,
            NoSuchAlgorithmException, MarshalException, XMLSignatureException {
        ByteArrayOutputStream signedDocument = simplePesInformation.getPesDestinationFile();
        Document pesDocument = simplePesInformation.getPesDocument();

        X509Certificate certificate = (X509Certificate) keyAlias.getCertChain()[0];
        PrivateKey privateKey = (PrivateKey) keyAlias.getKey();

        createOrUpdateInfoDemat(pesDocument);
        Node nsNode = DomUtils.createXMLDSigAndPesNamespaceNode(pesDocument);
        Node pesAllerNode = XPathAPI.selectSingleNode(pesDocument, "/n:PES_Aller", nsNode);
        prefixEtsi = DomUtils.recupPrefix(pesAllerNode, "http://uri.etsi.org/01903/v1.1.1#");
        prefixXmlDsig = DomUtils.recupPrefix(pesAllerNode, "http://www.w3.org/2000/09/xmldsig#");
        if (prefixEtsi == null) {
            prefixEtsi = "xad";
        } else {
            prefixEtsi = prefixEtsi.substring(prefixEtsi.lastIndexOf("xmlns:") + 6);
        }
        if (prefixXmlDsig == null) {
            prefixXmlDsig = "ds";
        } else {
            prefixXmlDsig = prefixXmlDsig.substring(prefixXmlDsig.lastIndexOf("xmlns:") + 6);
        }
        int signatureCnt = 1;

        NodeList bordereaux = XPathAPI.selectNodeList(pesDocument, "//Bordereau", nsNode);
        int nbbordereaux = bordereaux.getLength();
        for (int i = 0; i < nbbordereaux; i++) {
            Element bordereau = (Element) bordereaux.item(i);
            String signatureId = getSignatureID(signatureCnt);
            signatureCnt++;

            String bordereauID = null;
            String existingBordId = bordereau.getAttributeNS(null, "Id");
            if ((existingBordId != null) && (!existingBordId.isEmpty())) {
                bordereauID = existingBordId;
            } else {
                bordereauID = "BRD_" + signatureId;
                bordereau.setAttributeNS(null, "Id", bordereauID);
            }
            bordereau.setIdAttribute("Id", true);
            String signedRefURI = "#" + bordereauID;

            String keyInfoId = signatureId + "_KI";
            String signedPropertiesId = signatureId + "_SP";

            XMLSignatureFactory signatureFactory = XMLSignatureFactory.getInstance("DOM");
            List<Reference> referencesList = new ArrayList<Reference>();

            ArrayList<Transform> transformListWithEnveloped = new ArrayList<Transform>();
            transformListWithEnveloped.add(signatureFactory.newTransform(
                    "http://www.w3.org/2000/09/xmldsig#enveloped-signature", (TransformParameterSpec) null));
            transformListWithEnveloped.add(signatureFactory.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#",
                    (TransformParameterSpec) null));

            Reference contentReference = signatureFactory.newReference(signedRefURI,
                    signatureFactory.newDigestMethod("http://www.w3.org/2000/09/xmldsig#sha1", null),
                    transformListWithEnveloped, null, null);

            ArrayList<Transform> transformList = new ArrayList<Transform>();
            transformList.add(signatureFactory.newTransform("http://www.w3.org/2001/10/xml-exc-c14n#",
                    (TransformParameterSpec) null));

            Reference SignaturePropertiesReference = signatureFactory.newReference("#" + signedPropertiesId,
                    signatureFactory.newDigestMethod("http://www.w3.org/2000/09/xmldsig#sha1", null), transformList,
                    (String) null, (String) null);
            referencesList.add(contentReference);
            referencesList.add(SignaturePropertiesReference);

            String pkAlgo = privateKey.getAlgorithm();
            String algoId = null;
            if (pkAlgo.equals("RSA")) {
                algoId = "http://www.w3.org/2000/09/xmldsig#rsa-sha1";
            } else if (pkAlgo.equals("DSA")) {
                algoId = "http://www.w3.org/2000/09/xmldsig#dsa-sha1";
            }
            SignedInfo signedInfo = signatureFactory.newSignedInfo(
                    signatureFactory.newCanonicalizationMethod("http://www.w3.org/2001/10/xml-exc-c14n#",
                            (C14NMethodParameterSpec) null),
                    signatureFactory.newSignatureMethod(algoId, null), Collections.unmodifiableList(referencesList));
            KeyInfoFactory keyInfoFactory = signatureFactory.getKeyInfoFactory();

            X509Data x509Data = keyInfoFactory.newX509Data(Collections.singletonList(certificate));
            KeyInfo keyInfo = keyInfoFactory.newKeyInfo(Collections.singletonList(x509Data), keyInfoId);

            Element qualifPropertiesNode = createQualifyingPropertiesNode(pesDocument, signatureId, prefixEtsi,
                    prefixXmlDsig);
            Element signedPropertiesNode = createSignedPropertiesNode(pesDocument, signedPropertiesId, prefixEtsi,
                    prefixXmlDsig);
            Element signedSignaturePropertiesNode = createSignedSignaturePropertiesNode(pesDocument, prefixEtsi);
            signedSignaturePropertiesNode
                    .appendChild(createSigningTimePropertiesNode(pesDocument, xadesInfo.getSigningTime(), prefixEtsi));
            signedSignaturePropertiesNode.appendChild(
                    createSigningCertificatePropertiesNode(pesDocument, (X509Certificate) keyAlias.getCertChain()[0],
                            "http://www.w3.org/2000/09/xmldsig#sha1", prefixEtsi, prefixXmlDsig));
            signedSignaturePropertiesNode.appendChild(createSignaturePolicyIdentifierPropertiesNode(pesDocument,
                    signingPolicy, prefixEtsi, prefixXmlDsig));
            signedSignaturePropertiesNode.appendChild(
                    createSignatureProductionPlacePropertiesNode(pesDocument, xadesInfo.getSigProdPlaceCity(),
                            xadesInfo.getSigProdPlacePostalCode(), xadesInfo.getSigProdPlaceCountry(), prefixEtsi));
            signedSignaturePropertiesNode.appendChild(
                    createSignerRolePropertiesNode(pesDocument, xadesInfo.getSigClaimedRole(), prefixEtsi));
            signedPropertiesNode.appendChild(signedSignaturePropertiesNode);
            qualifPropertiesNode.appendChild(signedPropertiesNode);

            XMLObject xmlObject = signatureFactory.newXMLObject(
                    Collections.singletonList(new DOMStructure(qualifPropertiesNode)), signatureId + "_QI", null, null);
            XMLSignature xmlSignature = signatureFactory.newXMLSignature(signedInfo, keyInfo,
                    Collections.singletonList(xmlObject), signatureId, signatureId + "_SV");
            DOMSignContext context = createSignContext(privateKey, bordereau);

            context.setProperty("javax.xml.crypto.dsig.cacheReference", Boolean.TRUE);
            xmlSignature.sign(context);
            simplePesInformation.addSignatureElement(bordereau);
        }
        outputDOM(pesDocument, signedDocument, pesDocument.getXmlEncoding());
    }

    private static Element createXadesElement(Document document, String namespaceURI, String xadprefix, String dsprefix,
            String qualifiedName) {
        Element ret = document.createElementNS(namespaceURI, xadprefix + DEUX_POINT + qualifiedName);
        ret.setAttributeNS("http://www.w3.org/2000/xmlns/", "xmlns" + DEUX_POINT + xadprefix,
                "http://uri.etsi.org/01903/v1.1.1#");

        return ret;
    }

    public static void outputDOM(Document document, ByteArrayOutputStream signedDocument, String encoding)
            throws IOException, TransformerConfigurationException, TransformerException {
        String oldsep = System.getProperty("line.separator");
        System.setProperty("line.separator", "\n");
        try {
            StreamResult streamResult = new StreamResult(signedDocument);
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.setOutputProperty("encoding", encoding);
            transformer.setOutputProperty("method", "xml");

            transformer.transform(new DOMSource(document), streamResult);
        } finally {
            System.setProperty("line.separator", oldsep);
        }
    }

    private static Element createQualifyingPropertiesNode(Document doc, String target, String xadprefix,
            String dsprefix) {
        List<ElementAttribute1> tmp = new ArrayList<>();
        tmp.add(new ElementAttribute1("Target", target));
        Element qualifyingPropertiesElement = createXadesElement(doc, "http://uri.etsi.org/01903/v1.1.1#", xadprefix,
                dsprefix, "QualifyingProperties");
        qualifyingPropertiesElement.setAttributeNS(null, "Target", target);
        return qualifyingPropertiesElement;
    }

    private static Element createSignedPropertiesNode(Document doc, String id, String xadprefix, String dsprefix) {
        List<ElementAttribute1> tmp = new ArrayList<>();
        tmp.add(new ElementAttribute1("Id", id));

        Element signedPropertiesElement = createXadesElement(doc, "http://uri.etsi.org/01903/v1.1.1#", xadprefix,
                dsprefix, "SignedProperties");

        signedPropertiesElement.setAttributeNS(null, "Id", id);
        signedPropertiesElement.setIdAttribute("Id", true);
        return signedPropertiesElement;
    }

    private static Element createSignedSignaturePropertiesNode(Document doc, String prefix) {
        return DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SignedSignatureProperties", null, null);
    }

    private static Element createSigningTimePropertiesNode(Document doc, GregorianCalendar calendar, String prefix) {
        return DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#", prefix + DEUX_POINT + "SigningTime",
                null, DateUtils.utcFormat(calendar));
    }

    private static Element createSigningCertificatePropertiesNode(Document doc, X509Certificate signingCertificate,
            String digestMethod, String xadprefix, String dsprefix)
            throws CertificateEncodingException, NoSuchAlgorithmException, IOException {
        Element certElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "Cert", null, null);
        certElement.appendChild(
                createCertDigestPropertiesNode(doc, signingCertificate, digestMethod, xadprefix, dsprefix));
        certElement.appendChild(createIssuerSerialPropertiesNode(doc, signingCertificate, xadprefix, dsprefix));
        Element signingCertificateElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "SigningCertificate", null, null);
        signingCertificateElement.appendChild(certElement);
        return signingCertificateElement;
    }

    private static Element createCertDigestPropertiesNode(Document doc, X509Certificate signingCertificate,
            String digestMethod, String xadprefix, String dsprefix)
            throws CertificateEncodingException, NoSuchAlgorithmException, IOException {
        List<ElementAttribute1> tmp = new ArrayList<ElementAttribute1>();
        tmp.add(new ElementAttribute1("Algorithm", digestMethod));
        Element digestMethodElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "DigestMethod", tmp, null);

        Element digestValueElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "DigestValue", null,
                DigestUtils.xmlBase64Digest(signingCertificate.getEncoded(), digestMethod));

        Element certDigestElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "CertDigest", null, null);

        certDigestElement.appendChild(digestMethodElement);
        certDigestElement.appendChild(digestValueElement);
        return certDigestElement;
    }

    private static Element createIssuerSerialPropertiesNode(Document doc, X509Certificate signingCertificate,
            String xadprefix, String dsprefix) throws CertificateEncodingException {
        Element x509IssuerNameElement = DomUtils.createElementNS(doc, "http://www.w3.org/2000/09/xmldsig#",
                dsprefix + DEUX_POINT + "X509IssuerName", null, signingCertificate.getIssuerX500Principal().getName());

        Element x509SerialNumberElement = DomUtils.createElementNS(doc, "http://www.w3.org/2000/09/xmldsig#",
                dsprefix + DEUX_POINT + "X509SerialNumber", null, signingCertificate.getSerialNumber().toString());

        Element issuerSerialElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "IssuerSerial", null, null);

        issuerSerialElement.appendChild(x509IssuerNameElement);
        issuerSerialElement.appendChild(x509SerialNumberElement);
        return issuerSerialElement;
    }

    private static Element createSignaturePolicyIdentifierPropertiesNode(Document doc, SigningPolicy1 signingPolicy,
            String xadprefix, String dsprefix) throws CertificateEncodingException, IOException {
        Element signaturePolicyIdentifier = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "SignaturePolicyIdentifier", null, null);
        Element signaturePolicyId = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "SignaturePolicyId", null, null);
        signaturePolicyId.appendChild(createSigPolicyIdPropertiesNode(doc, signingPolicy, xadprefix));
        signaturePolicyId.appendChild(createSigPolicyHashPropertiesNode(doc, signingPolicy, xadprefix, dsprefix));
        signaturePolicyId.appendChild(createSigPolicyQualifiersPropertiesNode(doc, signingPolicy, xadprefix));
        signaturePolicyIdentifier.appendChild(signaturePolicyId);
        return signaturePolicyIdentifier;
    }

    private static Element createSigPolicyIdPropertiesNode(Document doc, SigningPolicy1 signingPolicy, String prefix) {
        Element sigPolicyId = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SigPolicyId", null, null);
        sigPolicyId.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "Identifier", null, signingPolicy.getIdentifier()));
        sigPolicyId.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "Description", null, signingPolicy.getDescription()));
        return sigPolicyId;
    }

    private static Element createSigPolicyHashPropertiesNode(Document doc, SigningPolicy1 signingPolicy,
            String xadprefix, String dsprefix) throws IOException {
        Element sigPolicyHash = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "SigPolicyHash", null, null);
        List<ElementAttribute1> tmp = new ArrayList<ElementAttribute1>();
        tmp.add(new ElementAttribute1("Algorithm", signingPolicy.getDigestMethod()));
        Element digestMethodElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "DigestMethod", tmp, null);

        sigPolicyHash.appendChild(digestMethodElement);

        Element digestValueElement = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                xadprefix + DEUX_POINT + "DigestValue", null, signingPolicy.getDigestValue());

        sigPolicyHash.appendChild(digestValueElement);
        return sigPolicyHash;
    }

    private static Element createSigPolicyQualifiersPropertiesNode(Document doc, SigningPolicy1 signingPolicy,
            String prefix) throws IOException {
        Element sigPolicyQualifiers = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SigPolicyQualifiers", null, null);
        Element sigPolicyQualifier = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SigPolicyQualifier", null, null);
        sigPolicyQualifier.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SPURI", null, signingPolicy.getSigPolicyQualifier()));
        sigPolicyQualifiers.appendChild(sigPolicyQualifier);
        return sigPolicyQualifiers;
    }

    private static Element createSignatureProductionPlacePropertiesNode(Document doc, String city, String postalCode,
            String countryName, String prefix) {
        Element signatureProductionPlace = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SignatureProductionPlace", null, null);
        signatureProductionPlace.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "City", null, city));
        signatureProductionPlace.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "PostalCode", null, postalCode));
        signatureProductionPlace.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "CountryName", null, countryName));
        return signatureProductionPlace;
    }

    private static Element createSignerRolePropertiesNode(Document doc, String claimedRole, String prefix) {
        Element signerRole = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "SignerRole", null, null);
        Element claimedRoles = DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "ClaimedRoles", null, null);
        claimedRoles.appendChild(DomUtils.createElementNS(doc, "http://uri.etsi.org/01903/v1.1.1#",
                prefix + DEUX_POINT + "ClaimedRole", null, claimedRole));
        signerRole.appendChild(claimedRoles);
        return signerRole;
    }

    private static void createOrUpdateInfoDemat(Document pesDocument)
            throws TransformerException, ParserConfigurationException, SAXException, IOException {
        Node namespace = DomUtils.createXMLDSigAndPesNamespaceNode(pesDocument);
        if (!DomUtils.updateNodeAttribute(pesDocument, "/n:PES_Aller/PES_DepenseAller/EnTeteDepense/InfoDematerialisee",
                "V", "1", namespace)) {
            Element enTeteDepense = DomUtils.recupElement(pesDocument, "/n:PES_Aller/PES_DepenseAller/EnTeteDepense",
                    namespace);
            if (enTeteDepense != null) {
                enTeteDepense.appendChild(DomUtils.createElement(pesDocument, "InfoDematerialisee",
                        Arrays.asList(new ElementAttribute1[] { new ElementAttribute1("V", "1") }), null));
            }
        }
        if (!DomUtils.updateNodeAttribute(pesDocument, "/n:PES_Aller/PES_RecetteAller/EnTeteRecette/InfoDematerialisee",
                "V", "1", namespace)) {
            Element enTeteRecette = DomUtils.recupElement(pesDocument, "/n:PES_Aller/PES_RecetteAller/EnTeteRecette",
                    namespace);
            if (enTeteRecette != null) {
                enTeteRecette.appendChild(DomUtils.createElement(pesDocument, "InfoDematerialisee",
                        Arrays.asList(new ElementAttribute1[] { new ElementAttribute1("V", "1") }), null));
            }
        }
    }

    public static DOMSignContext createSignContext(PrivateKey privateKey, Element e) {
        DOMSignContext signContext = new DOMSignContext(privateKey, e);
        signContext.putNamespacePrefix("http://uri.etsi.org/01903/v1.1.1#", prefixEtsi);
        signContext.putNamespacePrefix("http://www.w3.org/2000/09/xmldsig#", prefixXmlDsig);

        return signContext;
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.producer.SignatureGenerator
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */