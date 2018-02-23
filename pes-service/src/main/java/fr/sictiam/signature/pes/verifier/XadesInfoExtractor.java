package fr.sictiam.signature.pes.verifier;

import fr.sictiam.signature.utils.DateUtils;
import fr.sictiam.signature.utils.DomUtils;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.transform.TransformerException;

import java.util.GregorianCalendar;

public class XadesInfoExtractor {
    public XadesInfo1 process(XMLSignature xmlSignature) throws Exception {
        XadesInfo1 xadesInfo = null;
        for (int i = 0; i < xmlSignature.getObjectLength(); i++) {
            Element dsObjectElement = xmlSignature.getObjectItem(i).getElement();
            NodeList nodeList = dsObjectElement.getChildNodes();
            for (int n = 0; n < nodeList.getLength(); n++) {
                Node node = nodeList.item(n);
                if ((1 == node.getNodeType()) && ("QualifyingProperties".equals(node.getLocalName()))
                        && (node.getNamespaceURI().startsWith("http://uri.etsi.org/01903"))) {
                    if (xadesInfo != null) {
                        throw new Exception();
                    }
                    xadesInfo = extractXadesInfo((Element) node);
                }
            }
        }
        if (xadesInfo == null) {
            throw new Exception();
        }
        return xadesInfo;
    }

    private XadesInfo1 extractXadesInfo(Element qualifyingPropertiesElement) {
        XadesInfo1 xadesInfo = new XadesInfo1();
        try {
            Node signingTimeNode = XPathAPI.selectSingleNode(qualifyingPropertiesElement, ".//xad:SigningTime");
            xadesInfo.setSigningTimeAsString(signingTimeNode != null ? signingTimeNode.getTextContent().trim() : null);
            xadesInfo.setSigningTime(
                    signingTimeNode != null ? DateUtils.parseUtc(signingTimeNode.getTextContent().trim()) : null);

            Element signaturePolicyIdElement = (Element) XPathAPI.selectSingleNode(qualifyingPropertiesElement,
                    ".//xad:SignaturePolicyId");
            xadesInfo.setSigPolicyId(
                    DomUtils.getFCEC(DomUtils.getFCE(signaturePolicyIdElement, "SigPolicyId"), "Identifier"));
            xadesInfo.setSigPolicyDescription(
                    DomUtils.getFCEC(DomUtils.getFCE(signaturePolicyIdElement, "SigPolicyId"), "Description"));
            xadesInfo.setSigPolicyHashDigestMethod(DomUtils
                    .getFCEA(DomUtils.getFCE(signaturePolicyIdElement, "SigPolicyHash"), "DigestMethod", "Algorithm"));
            xadesInfo.setSigPolicyHashDigestValue(
                    DomUtils.getFCEC(DomUtils.getFCE(signaturePolicyIdElement, "SigPolicyHash"), "DigestValue"));
            xadesInfo.setSigPolicyQualifier(DomUtils.getFCEC(DomUtils.getFCE(
                    DomUtils.getFCE(signaturePolicyIdElement, "SigPolicyQualifiers"), "SigPolicyQualifier"), "SPURI"));

            Element sigProductionPlace = (Element) XPathAPI.selectSingleNode(qualifyingPropertiesElement,
                    ".//xad:SignatureProductionPlace");
            xadesInfo.setSigProdPlaceCity(DomUtils.getFCEC(sigProductionPlace, "City"));
            xadesInfo.setSigProdPlaceCountry(DomUtils.getFCEC(sigProductionPlace, "CountryName"));
            xadesInfo.setSigProdPlacePostalCode(DomUtils.getFCEC(sigProductionPlace, "PostalCode"));

            Element sigCertificate = (Element) XPathAPI.selectSingleNode(qualifyingPropertiesElement, ".//xad:Cert");
            xadesInfo.setSigCertDigestMethod(
                    DomUtils.getFCEA(DomUtils.getFCE(sigCertificate, "CertDigest"), "DigestMethod", "Algorithm"));
            xadesInfo.setSigCertDigestValue(
                    DomUtils.getFCEC(DomUtils.getFCE(sigCertificate, "CertDigest"), "DigestValue"));
            xadesInfo.setSigCertIssuerName(
                    DomUtils.getFCEC(DomUtils.getFCE(sigCertificate, "IssuerSerial"), "X509IssuerName"));
            xadesInfo.setSigCertIssuerSerial(
                    DomUtils.getFCEC(DomUtils.getFCE(sigCertificate, "IssuerSerial"), "X509SerialNumber"));
            xadesInfo.setSigTarget(qualifyingPropertiesElement.getAttribute("Target"));

            Element sigSignerRole = (Element) XPathAPI.selectSingleNode(qualifyingPropertiesElement,
                    ".//xad:SignerRole/xad:ClaimedRoles");
            xadesInfo.setSigClaimedRole(DomUtils.getFCEC(sigSignerRole, "ClaimedRole"));
        } catch (TransformerException te) {
            throw new UnExpectedException(te);
        }
        return xadesInfo;
    }

    public static class XadesInfo1 {
        private String signingTimeAsString;
        private GregorianCalendar signingTime;
        private String sigPolicyId;
        private String sigPolicyDescription;
        private String sigPolicyHashDigestMethod;
        private String sigPolicyHashDigestValue;
        private String sigPolicyQualifier;
        private String sigProdPlaceCity;
        private String sigProdPlacePostalCode;
        private String sigProdPlaceCountry;
        private String sigCertDigestMethod;
        private String sigCertDigestValue;
        private String sigCertIssuerName;
        private String sigCertIssuerSerial;
        private String sigTarget;
        private String sigClaimedRole;

        public String getSigningTimeAsString() {
            return signingTimeAsString;
        }

        public void setSigningTimeAsString(String signingTimeAsString) {
            this.signingTimeAsString = signingTimeAsString;
        }

        public GregorianCalendar getSigningTime() {
            return signingTime;
        }

        public void setSigningTime(GregorianCalendar signingTime) {
            this.signingTime = signingTime;
        }

        public String getSigPolicyId() {
            return sigPolicyId;
        }

        public void setSigPolicyId(String signaturePolicyId) {
            sigPolicyId = signaturePolicyId;
        }

        public String getSigPolicyDescription() {
            return sigPolicyDescription;
        }

        public void setSigPolicyDescription(String signaturePolicyDescription) {
            sigPolicyDescription = signaturePolicyDescription;
        }

        public String getSigPolicyHashDigestMethod() {
            return sigPolicyHashDigestMethod;
        }

        public void setSigPolicyHashDigestMethod(String signaturePolicyHashDigestMethod) {
            sigPolicyHashDigestMethod = signaturePolicyHashDigestMethod;
        }

        public String getSigPolicyHashDigestValue() {
            return sigPolicyHashDigestValue;
        }

        public void setSigPolicyHashDigestValue(String signaturePolicyHashDigestValue) {
            sigPolicyHashDigestValue = signaturePolicyHashDigestValue;
        }

        public String getSigProdPlaceCity() {
            return sigProdPlaceCity;
        }

        public void setSigProdPlaceCity(String sigProdPlaceCity) {
            this.sigProdPlaceCity = sigProdPlaceCity;
        }

        public String getSigProdPlaceCountry() {
            return sigProdPlaceCountry;
        }

        public void setSigProdPlaceCountry(String sigProdPlaceCountry) {
            this.sigProdPlaceCountry = sigProdPlaceCountry;
        }

        public String getSigProdPlacePostalCode() {
            return sigProdPlacePostalCode;
        }

        public void setSigProdPlacePostalCode(String sigProdPlacePostalCode) {
            this.sigProdPlacePostalCode = sigProdPlacePostalCode;
        }

        public String getSigCertDigestMethod() {
            return sigCertDigestMethod;
        }

        public void setSigCertDigestMethod(String sigCertDigestMethod) {
            this.sigCertDigestMethod = sigCertDigestMethod;
        }

        public String getSigCertDigestValue() {
            return sigCertDigestValue;
        }

        public void setSigCertDigestValue(String sigCertDigestValue) {
            this.sigCertDigestValue = sigCertDigestValue;
        }

        public String getSigCertIssuerName() {
            return sigCertIssuerName;
        }

        public void setSigCertIssuerName(String sigCertIssuerName) {
            this.sigCertIssuerName = sigCertIssuerName;
        }

        public String getSigCertIssuerSerial() {
            return sigCertIssuerSerial;
        }

        public void setSigCertIssuerSerial(String sigCertIssuerSerial) {
            this.sigCertIssuerSerial = sigCertIssuerSerial;
        }

        public String getSigTarget() {
            return sigTarget;
        }

        public void setSigTarget(String sigTarget) {
            this.sigTarget = sigTarget;
        }

        public String getSigClaimedRole() {
            return sigClaimedRole;
        }

        public void setSigClaimedRole(String sigClaimedRole) {
            this.sigClaimedRole = sigClaimedRole;
        }

        public String getSigPolicyQualifier() {
            return sigPolicyQualifier;
        }

        public void setSigPolicyQualifier(String sigPolicyQualifier) {
            this.sigPolicyQualifier = sigPolicyQualifier;
        }
    }
}

/*
 * Location: Qualified Name: com.axyus.signature.pes.verifier.XadesInfoExtractor
 * Java Class Version: 6 (50.0) JD-Core Version: 0.7.1
 */